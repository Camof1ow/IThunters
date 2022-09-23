package com.example.itmonster.service;

import com.example.itmonster.domain.Member;
import com.example.itmonster.domain.RoleEnum;
import com.example.itmonster.exceptionHandler.CustomException;
import com.example.itmonster.exceptionHandler.ErrorCode;
import com.example.itmonster.repository.MemberRepository;
import com.example.itmonster.security.jwt.JwtTokenUtils;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GoogleOAuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final HttpServletResponse response;

    @Transactional
    public String login( OAuth2User oAuth2User , HttpServletResponse response ) {
        if( oAuth2User == null ){
            throw new CustomException( ErrorCode.OAUTH2USER_NULL );
        }
        // TODO: 1. 회원이 아니라면 회원 가입을 시켜준다.

        String email = oAuth2User.getAttribute("email") ;
        String name =  oAuth2User.getAttribute("name") ;
        String imgUrl = oAuth2User.getAttribute("picture");
        String password = passwordEncoder.encode( UUID.randomUUID().toString() );

        if( !memberRepository.existsByEmail( email ) ){
            StringBuilder nickname = new StringBuilder( name );
            if (memberRepository.existsByNickname(nickname.toString())) {
                Random rnd = new Random();
                StringBuilder rdNick = new StringBuilder();
                for (int i = 0; i < 8; i++) {
                    rdNick.append(rnd.nextInt(10));
                    nickname.append(rdNick);
                }
            }
            //임시 전화번호 추가
            String dummyNumber = "";
            long random = (long)(Math.random() * (99999999999L - 10000000000L + 1)) + 10000000000L;

            Member member = Member.builder()
                .email( email )
                .nickname( nickname.toString() )
                .password( password )
                .role( RoleEnum.USER )
                .phoneNumber(dummyNumber+random)
                .profileImg( imgUrl )
                .build();
            memberRepository.save(member);
        }
        String token = JwtTokenUtils.generateJwtTokenByOAuth2(oAuth2User);
        response.setContentType("application/json; charset=utf-8");
        response.addHeader("Authorization", "BEARER" + " " + token);

        //TODO: 2. token 을 생성해준다.
        return token;
    }

    public void request() throws IOException {
        response.sendRedirect("https://g10000.shop/oauth2/authorization/google");
    }
}
