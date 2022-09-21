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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FindUserService {

    private final MemberRepository memberRepository;

    private final SmsService smsService;

    private final RedisUtil redisUtil;

    // 인증번호 발송
    public Boolean sendSmsForFindUsername(SmsRequestDto smsRequestDto) throws NoSuchAlgorithmException, InvalidKeyException {
        if(memberRepository.findByPhoneNumber(smsRequestDto.getPhoneNumber()).isEmpty()){
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        String phoneNum = smsRequestDto.getPhoneNumber();
        smsService.sendSms(phoneNum);
        return true;
    }

    public ResponseEntity<String> findUsername(SmsRequestDto smsRequestDto) {
        Member member = memberRepository.findByPhoneNumber(smsRequestDto.getPhoneNumber())
                .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_MEMBER));
        //String authNum = smsRequestDto.getAuthNo();
        String authNum = redisUtil.getData(smsRequestDto.getPhoneNumber());

        if (!Objects.equals(authNum, smsRequestDto.getPhoneNumber())) {
            return new ResponseEntity<>("인증번호가 일치하지 않습니다.", HttpStatus.OK);
        }
        return new ResponseEntity<>(member.getEmail(), HttpStatus.OK);
    }


}

//1.핸드폰 번호 받기
//2. SMS 인증
//3. 1과 2를 비교해서 동일할때
//4. 유저아이디를 프론트로

