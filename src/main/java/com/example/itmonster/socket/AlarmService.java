package com.example.itmonster.socket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlarmService {

    private final SimpMessageSendingOperations messagingTemplate;

    public void sendAlarm(AlarmDto alarmDto){
        messagingTemplate.convertAndSend("/sub/members/" + alarmDto.getReceiverId(), alarmDto);
    }
}
