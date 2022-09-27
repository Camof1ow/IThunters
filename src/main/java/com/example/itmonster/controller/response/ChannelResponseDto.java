package com.example.itmonster.controller.response;

import com.example.itmonster.domain.Channel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChannelResponseDto {
    private Long id;
    private String channelName;
    private String lastMessage;
    private String imgUrl;
}
