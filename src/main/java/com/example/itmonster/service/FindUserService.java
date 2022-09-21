package com.example.itmonster.service;

import com.example.itmonster.controller.request.SmsRequestDto;
import com.example.itmonster.domain.Member;
import com.example.itmonster.exceptionHandler.CustomException;
import com.example.itmonster.exceptionHandler.ErrorCode;
import com.example.itmonster.repository.MemberRepository;
import com.example.itmonster.service.SmsService;
import com.example.itmonster.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FindUserService {

    private final MemberRepository memberRepository;

    private final SmsService smsService;

    private final RedisUtil redisUtil;

    // 인증번호 발송
    public ResponseEntity<String> sendAuth(SmsRequestDto smsRequestDto) throws NoSuchAlgorithmException, InvalidKeyException {
        Member member = memberRepository.findByPhoneNum(smsRequestDto.getPhoneNo())
                .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_MEMBER));
        String phoneNum = smsRequestDto.getPhoneNo();
        smsService.sendSms(phoneNum, member.getId());

        return new ResponseEntity<String>("인증번호가 발송되었습니다.", HttpStatus.OK);
    }

    public ResponseEntity<String> findUsername(SmsRequestDto smsRequestDto) {
        Member member = memberRepository.findByPhoneNum(smsRequestDto.getPhoneNo())
                .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_MEMBER));
        //String authNum = smsRequestDto.getAuthNo();
        String authNum = redisUtil.getData(member.getId().toString());

        if (!Objects.equals(authNum, smsRequestDto.getAuthNo())) {
            return new ResponseEntity<String>("인증번호가 일치하지 않습니다.", HttpStatus.OK);
        }
        return new ResponseEntity<String>(member.getEmail(), HttpStatus.OK);
    }


}

//1.핸드폰 번호 받기
//2. SMS 인증
//3. 1과 2를 비교해서 동일할때
//4. 유저아이디를 프론트로

