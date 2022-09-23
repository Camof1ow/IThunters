package com.example.itmonster.service;

import com.example.itmonster.controller.request.SmsRequestDto;
import com.example.itmonster.domain.Member;
import com.example.itmonster.exceptionHandler.CustomException;
import com.example.itmonster.exceptionHandler.ErrorCode;
import com.example.itmonster.repository.MemberRepository;
import com.example.itmonster.utils.RedisUtil;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FindUserService {

    private final MemberRepository memberRepository;

    private final SmsService smsService;

    private final RedisUtil redisUtil;


    String phoneNumPattern = "^(\\d{11})$"; // 11자리 숫자
    // 인증번호 발송
    public String sendSmsForFindUsername(SmsRequestDto smsRequestDto) throws NoSuchAlgorithmException, InvalidKeyException {
        checkPhoneNumber(smsRequestDto.getPhoneNumber()); // 입력전화번호 유효성 검사
        if(!memberRepository.existsByPhoneNumber(smsRequestDto.getPhoneNumber())){
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        String response = smsService.sendSms(smsRequestDto.getPhoneNumber());
        if(response.contains("errors")){throw new CustomException(ErrorCode.FAILED_MESSAGE);} // 메시지 발송실패시 예외처리
        return response;
    }

    public String findUsername(SmsRequestDto smsRequestDto) {
        checkPhoneNumber(smsRequestDto.getPhoneNumber()); // 입력전화번호 유효성 검사
        String authNum = redisUtil.getData(smsRequestDto.getPhoneNumber());
        Member member = memberRepository.findByPhoneNumber(smsRequestDto.getPhoneNumber())
                .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_MEMBER));
        if (!Objects.equals(authNum, smsRequestDto.getAuthNumber())) {
            throw new CustomException(ErrorCode.FAILED_VERIFYING_AUTH);
        }
        return member.getEmail();
    }

    public void checkPhoneNumber(String phoneNum) {
        if (phoneNum == null) throw new CustomException(ErrorCode.EMPTY_PHONENUMBER);
        if (phoneNum.equals("")) throw new CustomException(ErrorCode.EMPTY_PHONENUMBER);
        if (phoneNum.length() != 11) throw new CustomException(ErrorCode.PHONENUMBER_LENGTH);
        if (!Pattern.matches(phoneNumPattern, phoneNum)) throw new CustomException(ErrorCode.PHONENUMBER_WRONG);

    }


}

//1.핸드폰 번호 받기
//2. SMS 인증
//3. 1과 2를 비교해서 동일할때
//4. 유저아이디를 프론트로

