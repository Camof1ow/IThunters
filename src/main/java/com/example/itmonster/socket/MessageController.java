package com.example.itmonster.socket;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @ResponseBody
    @GetMapping("/api/channels/{channelId}")
    public List<MessageResponseDto> readMessages(@PathVariable Long channelId) {
        return messageService.readMessages(channelId);
    }

    @GetMapping("/api/channels/{channelId}/test")
    public List<MessageResponseDto> readMessagesTest(@PathVariable Long channelId) {
        return messageService.readMessagesTest(channelId);
    }

    @MessageMapping(value = {"/channels/{channelId}"})
//    public void addMessage(@RequestBody MessageRequestDto messageRequestDto,
//        @DestinationVariable Long channelId,
//        @Header("Authorization") String token) {
//        System.out.println(token);
//        token = token.substring(7);
    public void sendMessage(@RequestBody MessageRequestDto messageRequestDto,
        @DestinationVariable Long channelId) {
        String token = messageRequestDto.getToken().substring(7); // 백엔드 테스트용
        messageService.sendMessage(messageRequestDto, channelId, token);
    }
}
