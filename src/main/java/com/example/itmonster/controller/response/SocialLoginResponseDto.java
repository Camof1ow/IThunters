package com.example.itmonster.controller.response;

import com.example.itmonster.domain.Member;
import lombok.Getter;

@Getter
public class SocialLoginResponseDto {
    //소셜로그인 시 body로 내려가는 사용자 정보

    private final Long userId;

    private final String nickname;

    private final String phoneNumber;

    private final boolean login;

    private final String profileImage;

    //소셜 로그인할 때 프론트에 내려주는 값
    public SocialLoginResponseDto(Member member, boolean login) {
        this.userId = member.getId();
        this.nickname = member.getNickname();
        this.phoneNumber = member.getPhoneNumber();
        this.login = login;  //login true/ false 상황
        this.profileImage = member.getProfileImg();
    }

}
