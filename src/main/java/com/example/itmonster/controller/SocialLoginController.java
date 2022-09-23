package com.example.itmonster.controller;

import com.example.itmonster.controller.response.ResponseDto;
import com.example.itmonster.exceptionHandler.CustomException;
import com.example.itmonster.exceptionHandler.ErrorCode;
import com.example.itmonster.service.GoogleOAuthService;
import com.example.itmonster.service.KakaoUserService;
import com.example.itmonster.service.NaverUserService;
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
    public ResponseEntity<HttpHeaders> kakaoLogin(@RequestParam String code, HttpServletResponse response) {

        try { // 회원가입 진행 성공시
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(kakaoUserService.kakaoLogin(code, response)));
            return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);

        } catch (Exception e) { // 에러나면 false
            throw new CustomException(ErrorCode.INVALID_KAKAO_LOGIN_ATTEMPT);
        }
    }

    //카카오 로그인 요청
    @GetMapping("/oauth/kakao")
    public ResponseEntity<HttpHeaders> kakaoLoginConntect(HttpSession session) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(kakaoUserService.connect(session)));
        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
    }

    // 네이버 로그인에 필요한 code와 state 생성하고 네이버 로그인 api 요청
    @GetMapping("/oauth/naver")
    public ResponseEntity<?> naverConnect(HttpSession session){
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(naverUserService.naverConnect(session)));
        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
    }

    //네이버 로그인
    @GetMapping("/oauth/main")
    public ResponseEntity<String> naverLogin(@RequestParam String code, @RequestParam String state, HttpServletResponse response) {
        try { // 회원가입 진행 성공시
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(naverUserService.naverLogin(code, state, response)));
            return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
        } catch (Exception e) { // 에러나면 false
            throw new CustomException(ErrorCode.INVALID_NAVER_LOGIN_ATTEMPT);
        }
    }

    // 구글로그인
    @GetMapping("/oauth/google")
    public ResponseEntity<String> login ( @AuthenticationPrincipal OAuth2User oAuth2User , HttpServletResponse response) {
        return ResponseEntity.ok( googleOAuthService.login( oAuth2User,response ) );
    }
    @GetMapping("/oauth/google/loginpage")
    public void loginPage () throws IOException {
        googleOAuthService.request();
    }
}
