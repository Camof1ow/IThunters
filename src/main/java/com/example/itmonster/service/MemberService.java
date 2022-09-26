package com.example.itmonster.service;

import com.example.itmonster.controller.request.MemberStacksDto;
import com.example.itmonster.controller.request.SignupRequestDto;
import com.example.itmonster.controller.request.SmsRequestDto;
import com.example.itmonster.controller.response.CompletedQuestDto;
import com.example.itmonster.controller.response.FollowResponseDto;
import com.example.itmonster.controller.response.MemberResponseDto;
import com.example.itmonster.controller.response.MyPageResponseDto;
import com.example.itmonster.controller.response.ResponseDto;
import com.example.itmonster.controller.response.SocialLoginResponseDto;
import com.example.itmonster.controller.response.StackDto;
import com.example.itmonster.domain.Folio;
import com.example.itmonster.domain.Follow;
import com.example.itmonster.domain.Member;
import com.example.itmonster.domain.RoleEnum;
import com.example.itmonster.domain.StackOfMember;
import com.example.itmonster.exceptionHandler.CustomException;
import com.example.itmonster.exceptionHandler.ErrorCode;
import com.example.itmonster.repository.FolioRepository;
import com.example.itmonster.repository.FollowRepository;
import com.example.itmonster.repository.MemberRepository;
import com.example.itmonster.repository.StackOfMemberRepository;
import com.example.itmonster.security.UserDetailsImpl;
import com.example.itmonster.utils.RedisUtil;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;
	private final StackOfMemberRepository stackOfMemberRepository;
	private final FollowRepository followRepository;
	private final PasswordEncoder passwordEncoder;
	private final AwsS3Service s3Service;
	private final FolioRepository folioRepository;
	private final RedisUtil redisUtil;
	private final SmsService smsService;


	String emailPattern = "^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$"; //이메일 정규식 패턴
	String nicknamePattern = "^[a-zA-Z0-9ㄱ-ㅎㅏ-ㅣ가-힣~!@#$%^&*]{2,8}$"; // 영어대소문자 , 한글 , 특수문자포함 2~8자까지
	String passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z~!@#$%^&*\\d]{8,20}$"; //  영어대소문자,숫자 포함 8자에서 20자;
	String phoneNumPattern = "^(\\d{11})$"; // 11자리 숫자

//    @Value("${spring.admin.token}") // 어드민 가입용
//    String ADMIN_TOKEN;

	@Transactional
	public String signupUser(SignupRequestDto requestDto) throws IOException {

		String profileUrl = s3Service.getSavedS3ImageUrl(requestDto.getProfileImage());
		if (!Objects.equals(redisUtil.getData(requestDto.getPhoneNumber()), "true")) {
			throw new CustomException(ErrorCode.FAILED_VERIFYING_PHONENUMBER);
		}

		checkEmailPattern(requestDto.getEmail());//username 정규식 맞지 않는 경우 오류메시지 전달
		checkNicknamePattern(requestDto.getNickname());//nickname 정규식 맞지 않는 경우 오류메시지 전달
		checkPasswordPattern(requestDto.getPassword());//password 정규식 맞지 않는 경우 오류메시지 전달
		checkPhoneNumber(requestDto.getPhoneNumber());

		String password = passwordEncoder.encode(requestDto.getPassword()); // 패스워드 암호화

		Member member = Member.builder()
			.email(requestDto.getEmail())
			.nickname(requestDto.getNickname())
			.password(password)
			.profileImg(profileUrl)
			.phoneNumber(requestDto.getPhoneNumber())
			.role(RoleEnum.USER)
			.className("")
			.build();
		memberRepository.save(member);

		// 빈 포트폴리오 생성
		folioRepository.save(Folio.builder()
			.title(member.getNickname() + "님의 포트폴리오입니다.")
			.member(member)
			.build());
		//인증된 전화번호 REDIS 삭제
		redisUtil.deleteData(requestDto.getPhoneNumber());

		return "회원가입을 축하합니다";
	}


	@Transactional
	public MemberResponseDto updateMemberInfo(Member member, SignupRequestDto requestDto)
		throws Exception {
		checkNicknamePattern(requestDto.getNickname()); //닉네임 유효성 검사
		if (memberRepository.existsByNickname(requestDto.getNickname())) { //닉네임 중복검사
			throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
		}

		Member updateUser = memberRepository.findById(member.getId())
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		String profileImg = s3Service.getSavedS3ImageUrl(requestDto.getProfileImage());

		updateUser.updateMemberInfo(requestDto.getNickname(), requestDto.getClassName(),
			profileImg);
		memberRepository.save(updateUser);

		return memberResponseBuild(updateUser);
	}


	@Transactional
	public ResponseDto<Boolean> followMember(Long memberId, Member me) {
		Member member = memberRepository.findById(memberId).orElseThrow(
			() -> new CustomException(ErrorCode.USER_NOT_FOUND)); // 팔로우 할 멤버 확인

		if (followRepository.findByFollowingIdAndMeId(  // 팔로우 한 적 없으면 팔로우등록
			memberId, me.getId()) == null) {
			followRepository.save(Follow.builder()
				.me(me)
				.following(member)
				.build());
			memberRepository.save(member);
			return ResponseDto.success(Boolean.TRUE);

		} else { //팔로우 한적 있으면 취소

			Follow follow = followRepository.findByFollowingIdAndMeId(
				memberId, me.getId());
			followRepository.delete(follow);
			memberRepository.save(member);

			return ResponseDto.success(Boolean.FALSE);
		}
	}

	@Transactional
	public ResponseDto<List<String>> addStack(MemberStacksDto memberStacksDto,
		Member member) { // 기술스택 추가
		stackOfMemberRepository.deleteAllByMemberId(member.getId());
		List<String> stacks = memberStacksDto.getStacks();
		for (String stackname : stacks) {
			StackOfMember stack = StackOfMember.builder()
				.stackName(stackname)
				.member(member).build();

			stackOfMemberRepository.save(stack);
		}

		return ResponseDto.success(stacks);
	}

	public List<StackDto> getStackList(Member member) {
		List<StackDto> stacks = new ArrayList<>();
		List<StackOfMember> stackOfMemberList = stackOfMemberRepository.findByMemberId(
			member.getId());
		if (stackOfMemberList.size() == 0L) {
			return stacks;
		}

		for (StackOfMember stack : stackOfMemberList) {
			stacks.add(new StackDto(stack.getStackName()));
		}

		return stacks;
	}

	@Cacheable(value = "monsterOfMonthCaching")
	@Transactional(readOnly = true)
	public List<MemberResponseDto> showTop3Following() {
		List<Member> members = memberRepository.findTop3ByOrderByFollowCounter();
		List<MemberResponseDto> responseDtoList = new ArrayList<>();
		for (Member member : members) {
			responseDtoList.add(memberResponseBuild(member));

		}
		return responseDtoList;
	}


	//username 중복체크
	public ResponseDto<String> checkUsername(SignupRequestDto requestDto) {
		checkEmailPattern(requestDto.getEmail());
		if (memberRepository.existsByEmail(requestDto.getEmail())) {
			throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
		}
		return ResponseDto.success("사용가능한 이메일입니다.");
	}

	public ResponseDto<String> checkNickname(SignupRequestDto requestDto) {
		checkNicknamePattern(requestDto.getNickname());
		if (memberRepository.existsByNickname(requestDto.getNickname())) {
			throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
		}
		return ResponseDto.success("사용 가능한 닉네임입니다.");
	}


	public MemberResponseDto memberInfo(Member member) {

		return memberResponseBuild(member);
	}

	public MyPageResponseDto getMyPage(Long memberId) {
		Member member = memberRepository.findById(memberId).orElseThrow(
			() -> new CustomException(ErrorCode.USER_NOT_FOUND));
		Folio folio = folioRepository.findByMemberId(memberId);
		List<CompletedQuestDto> completedQuestDtos = new ArrayList<>();
//      완료된 퀘스트 가져오기 로직
//        for(CompletedQuestDto completedQuestDto : completedQuestDtos){
//          completedQuestDtos.add(CompletedQuestDto.builder()
//              .questId()
//              .questTitle().build())
//
//        }

		return MyPageResponseDto.builder()
			.memberId(memberId)
			.profileUrl(member.getProfileImg())
			.stackList(getStackList(member))
			.title(folio.getTitle())
			.notionUrl(folio.getNotionUrl())
			.githubUrl(folio.getGithubUrl())
			.blogUrl(folio.getBlogUrl())
			.completedQuestList(completedQuestDtos)
			.build();
	}


	//소셜로그인 사용자 정보 조회
	public ResponseDto<SocialLoginResponseDto> socialUserInfo(UserDetailsImpl userDetails) {
		//로그인 한 user 정보 검색
		Member member = memberRepository.findBySocialId(userDetails.getMember().getSocialId())
			.orElseThrow(
				() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		//찾은 user엔티티를 dto로 변환해서 반환하기
		SocialLoginResponseDto socialLoginResponseDto = new SocialLoginResponseDto(member, true);
		return ResponseDto.success(socialLoginResponseDto);
	}


	public void checkEmailPattern(String email) {
		if (email == null) {
			throw new CustomException(ErrorCode.EMPTY_EMAIL);
		}
		if (email.equals("")) {
			throw new CustomException(ErrorCode.EMPTY_EMAIL);
		}
		if (!Pattern.matches(emailPattern, email)) {
			throw new CustomException(ErrorCode.EMAIL_WRONG);
		}
		if (memberRepository.findByEmail(email).isPresent()) {
			throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
		}
	}


	public void checkPasswordPattern(String password) {
		if (password == null) {
			throw new CustomException(ErrorCode.EMPTY_PASSWORD);
		}
		if (password.equals("")) {
			throw new CustomException(ErrorCode.EMPTY_PASSWORD);
		}
		if (8 > password.length() || 20 < password.length()) {
			throw new CustomException(ErrorCode.PASSWORD_LEGNTH);
		}
		if (!Pattern.matches(passwordPattern, password)) {
			throw new CustomException(ErrorCode.PASSWORD_WRONG);
		}
	}


	public void checkNicknamePattern(String nickname) {
		if (nickname == null) {
			throw new CustomException(ErrorCode.EMPTY_NICKNAME);
		}
		if (nickname.equals("")) {
			throw new CustomException(ErrorCode.EMPTY_NICKNAME);
		}
		if (2 > nickname.length() || 8 < nickname.length()) {
			throw new CustomException(ErrorCode.NICKNAME_LEGNTH);
		}
		if (!Pattern.matches(nicknamePattern, nickname)) {
			throw new CustomException(ErrorCode.NICKNAME_WRONG);
		}
	}

	public void checkPhoneNumber(String phoneNum) {
		if (phoneNum == null) {
			throw new CustomException(ErrorCode.EMPTY_PHONENUMBER);
		}
		if (phoneNum.equals("")) {
			throw new CustomException(ErrorCode.EMPTY_PHONENUMBER);
		}
		if (phoneNum.length() != 11) {
			throw new CustomException(ErrorCode.PHONENUMBER_LENGTH);
		}
		if (!Pattern.matches(phoneNumPattern, phoneNum)) {
			throw new CustomException(ErrorCode.PHONENUMBER_WRONG);
		}

	}

	@CacheEvict(value = "monsterOfMonthCaching", allEntries = true)
	public void deleteCacheTest() {
	}

	@CacheEvict(value = "monsterOfMonthCaching", allEntries = true)
	@Scheduled(cron = "0 0 0 * * *")
	public void deleteCache() {
	}


	public ResponseDto<String> sendSmsForSignup(SmsRequestDto requestDto)
		throws NoSuchAlgorithmException, InvalidKeyException {
		if (memberRepository.existsByPhoneNumber(requestDto.getPhoneNumber())) {
			throw new CustomException(ErrorCode.DUPLICATE_PHONENUMBER);
		}
		checkPhoneNumber(requestDto.getPhoneNumber());
		String response = smsService.sendSms(requestDto.getPhoneNumber());
		if (response.contains("errors")) {
			throw new CustomException(ErrorCode.FAILED_MESSAGE);
		}
		return ResponseDto.success(response);
	}

	public Boolean confirmPhoneNumber(SmsRequestDto requestDto) {
		String phoneNumber = requestDto.getPhoneNumber();
		if (Objects.equals(redisUtil.getData(phoneNumber), "true")) {
			return Boolean.TRUE;
		}// 이미 인증번호 인증을 마친 경우

		if (!Objects.equals(redisUtil.getData(phoneNumber), requestDto.getAuthNumber())) {
			throw new CustomException(ErrorCode.FAILED_VERIFYING_AUTH);
		}// 인증번호가 일치하지 않은경우

		redisUtil.deleteData(requestDto.getPhoneNumber());
		redisUtil.setDataExpire(phoneNumber, "true", 300);
		return Boolean.TRUE;
	}


	public MemberResponseDto memberResponseBuild(Member member) {
		return MemberResponseDto.builder()
			.id(member.getId())
			.nickname(member.getNickname())
			.profileImage(member.getProfileImg())
			.stacks(getStackList(member))
			.className(member.getClassName())
			.followCnt(member.getFollowCounter())
			.folioTitle(folioRepository.findByMemberId(member.getId()).getTitle())
			.build();
	}
}

//로그인 후 관리자 권한 얻을 수 있는 API 관리자 접근 가능 페이지 없슴
//    public ResponseEntity adminAuthorization(AdminRequestDto requestDto, UserDetailsImpl userDetails) {
//        // 사용자 ROLE 확인
//        UserRoleEnum role = UserRoleEnum.USER;
//        if (requestDto.isAdmin()) {
//            if (!requestDto.getAdminToken().equals(ADMIN_TOKEN)) {
//                throw new CustomException(ErrorCode.INVALID_AUTHORITY_WRONG); // 토큰값이 틀림
//            }
//            role = UserRoleEnum.ADMIN;
//        }
//
//        //역할 변경
//        userDetails.getUser().setRole(role);
//        //변경된 역할 저장
//        userRepository.save(userDetails.getUser());
//        return new ResponseEntity("관리자 권한으로 변경되었습니다", HttpStatus.OK);
//    }




