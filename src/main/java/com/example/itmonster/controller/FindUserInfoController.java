package com.example.itmonster.controller;


import com.example.itmonster.controller.request.SmsRequestDto;
import com.example.itmonster.service.FindUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequiredArgsConstructor
public class FindUserInfoController {

    private final FindUserService findUserService;


    @PostMapping("/api/members/sendAuth")
    public ResponseEntity<String> sendAuth(@RequestBody SmsRequestDto smsRequestDto) throws NoSuchAlgorithmException, InvalidKeyException {
        return findUserService.sendAuth(smsRequestDto);
    }

    @PostMapping("/api/members/findUsername")
    public ResponseEntity<String> findUsername(@RequestBody SmsRequestDto smsRequestDto) {
        return findUserService.findUsername(smsRequestDto);
    }
}
