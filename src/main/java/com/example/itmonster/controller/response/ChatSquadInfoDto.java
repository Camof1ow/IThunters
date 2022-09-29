package com.example.itmonster.controller.response;

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

}
