package com.example.itmonster.controller;

import com.example.itmonster.controller.response.ResponseDto;
import com.example.itmonster.exceptionHandler.CustomException;
import com.example.itmonster.exceptionHandler.ErrorCode;
import com.example.itmonster.service.GoogleOAuthService;
import com.example.itmonster.service.KakaoUserService;
import com.example.itmonster.service.NaverUserService;
import java.io.IOException;
import java.net.URI;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class SocialLoginController {

    private final KakaoUserService kakaoUserService;
    private final GoogleOAuthService googleOAuthService;
    private final NaverUserService naverUserService;

    //카카오 로그인
    @GetMapping("/oauth/kakao/callback")
    public ResponseDto<String> kakaoLogin(@RequestParam String code, HttpServletResponse response) {

        try { // 회원가입 진행 성공시
            return ResponseDto.success(kakaoUserService.kakaoLogin(code, response));

        } catch (Exception e) { // 에러나면 false
            throw new CustomException(ErrorCode.INVALID_KAKAO_LOGIN_ATTEMPT);
        }
    }


    // 네이버 로그인에 필요한 code와 state 생성하고 네이버 로그인 api 요청
    @GetMapping("/oauth/naver")
    public String naverConnect(HttpSession session){
        return naverUserService.naverConnect(session);
    }

    //네이버 로그인
    @GetMapping("/oauth/naver/callback")
    public ResponseDto<String> naverLogin(@RequestParam String code, @RequestParam String state, HttpServletResponse response) {
        try { // 회원가입 진행 성공시
            return ResponseDto.success(naverUserService.naverLogin(code, state, response));

        } catch (Exception e) { // 에러나면 false
            throw new CustomException(ErrorCode.INVALID_NAVER_LOGIN_ATTEMPT);
        }
    }

    // 구글로그인
    @GetMapping("/oauth/google")
    public ResponseEntity<HttpHeaders> login ( @AuthenticationPrincipal OAuth2User oAuth2User , HttpServletResponse response ){
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("http://localhost:3000/auth?accessToken=" + googleOAuthService.login( oAuth2User,response )));
        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
    }

}
