package com.example.itmonster.controller;

import com.example.itmonster.controller.request.MemberStacksDto;
import com.example.itmonster.controller.request.SignupRequestDto;
import com.example.itmonster.controller.request.SmsRequestDto;
import com.example.itmonster.controller.response.MemberResponseDto;
import com.example.itmonster.controller.response.ResponseDto;
import com.example.itmonster.controller.response.SocialLoginResponseDto;
import com.example.itmonster.security.UserDetailsImpl;
import com.example.itmonster.service.MemberService;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;

	//회원가입
	@PostMapping("api/members/signup")
	public ResponseDto<String> signupUser(@RequestBody SignupRequestDto requestDto)
		throws IOException {
		return ResponseDto.success(memberService.signupUser(requestDto));
	}

	//username 중복체크
	@PostMapping("/api/members/checkId")
	public ResponseDto<String> checkUsername(@RequestBody SignupRequestDto requestDto) {
		return memberService.checkUsername(requestDto);
	}

	//닉네임 중복체크
	@PostMapping("/api/members/checkNickname")
	public ResponseDto<String> checkNickname(@RequestBody SignupRequestDto requestDto) {
		return memberService.checkNickname(requestDto);
	}

	//멤버 팔로우
	@PostMapping("/api/members/{memberId}/follow")
	public ResponseEntity followMember(@PathVariable Long memberId,
		@AuthenticationPrincipal UserDetailsImpl userDetails) {
		return memberService.followMember(memberId, userDetails.getMember());
	}

	// 스택 추가
	@PostMapping("/api/members/addStack")
	public ResponseEntity<String> addStack(@RequestBody MemberStacksDto memberStacksDto,
		@AuthenticationPrincipal UserDetailsImpl userDetails) {
		return memberService.addStack(memberStacksDto, userDetails.getMember());
	}

	//이달의 회원 팔로우기준 top3
	@GetMapping("/api/monster/month")
	public ResponseEntity<List> showTop3Following() {
		return ResponseEntity.ok(memberService.showTop3Following());
	}

	// 현재 로그인된 유저 정보 확인(임시)
	@GetMapping("/api/members/status")
	public ResponseEntity<MemberResponseDto> memberInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
		return ResponseEntity.ok(memberService.memberInfo(userDetails.getMember()));
	}

	@PostMapping("/api/members/sendSmsForSignup")
	public ResponseDto<String> sendSmsForSignup(@RequestBody SmsRequestDto requestDto)
		throws NoSuchAlgorithmException, InvalidKeyException {
		return memberService.sendSmsForSignup(requestDto);
	}

	@PostMapping("/api/members/confirmPhoneNumber")
	public ResponseDto<Boolean> confirmPhoneNumber(@RequestBody SmsRequestDto requestDto){
		return ResponseDto.success(memberService.confirmPhoneNumber(requestDto));
	}

	@PostMapping("/api/members/deleteCache")
	public void deleteCacheTest(){
		memberService.deleteCacheTest();
	}

	//로그인 후 관리자 권한 얻을 수 있는 API
//  @PutMapping("/api/signup/admin")
//  public ResponseEntity adminAuthorization(@RequestBody AdminRequestDto requestDto,
//                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
//    return memberService.adminAuthorization(requestDto, userDetails);
//  }

	//소셜로그인 사용자 정보 조회
	@GetMapping("/social/member/islogin")
	public ResponseDto<SocialLoginResponseDto> socialUserInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
		return memberService.socialUserInfo(userDetails);
	}

	@GetMapping("/api/myPage/{memberId}")
	public ResponseEntity getMyPage(@PathVariable Long memberId){
		return ResponseEntity.ok(memberService.getMyPage(memberId));
	}


}
