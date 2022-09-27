package com.example.itmonster.controller.response;

import com.example.itmonster.domain.Member;
import lombok.Getter;

@Getter
public class SquadMemberDto {
    private final Long memberId;
    private final String nickname;
    private final String profileImg;

    public SquadMemberDto(Member member){
        this.memberId = member.getId();
        this.nickname = member.getNickname();
        this.profileImg = member.getProfileImg();
    }
}
