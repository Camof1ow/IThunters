package com.example.itmonster.controller.response;

import com.example.itmonster.domain.Member;
import java.io.Serializable;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatSquadInfoDto implements Serializable {
    private Long channelId;
    private String channelName;
    private List<SquadMemberDto> squadMembers;
    private Long leaderId;
}
