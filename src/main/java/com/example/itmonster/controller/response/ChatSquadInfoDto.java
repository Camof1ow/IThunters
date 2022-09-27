package com.example.itmonster.controller.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatSquadInfoDto {
    private Long channelId;
    private String channelName;
    private List<SquadMemberDto> squadMembers;

}
