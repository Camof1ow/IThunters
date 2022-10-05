package com.example.itmonster.socket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AlarmController {

    private final SimpMessageSendingOperations messagingTemplate;

    @MessageMapping("/members/{memberId}")
    public void sendAlarm(@DestinationVariable Long memberId) {
        messagingTemplate.convertAndSend("/sub/members/" + memberId,
            "alarm socket connection completed.");
    }
}
