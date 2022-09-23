package com.example.itmonster.controller;


import com.example.itmonster.controller.request.SmsRequestDto;
import com.example.itmonster.controller.response.ResponseDto;
import com.example.itmonster.service.FindUserService;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FindUserInfoController {

    private final FindUserService findUserService;


    @PostMapping("/api/members/sendAuth")
    public ResponseDto<String> sendAuth(@RequestBody SmsRequestDto smsRequestDto) throws NoSuchAlgorithmException, InvalidKeyException {
        return ResponseDto.success(findUserService.sendSmsForFindUsername(smsRequestDto));
    }

    @PostMapping("/api/members/findUsername")
    public ResponseDto<String> findUsername(@RequestBody SmsRequestDto smsRequestDto) {
        return ResponseDto.success(findUserService.findUsername(smsRequestDto));
    }
}
