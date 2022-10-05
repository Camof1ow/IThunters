package com.example.itmonster.notification;

import com.example.itmonster.domain.Member;
import com.example.itmonster.exceptionHandler.CustomException;
import com.example.itmonster.exceptionHandler.ErrorCode;
import com.example.itmonster.repository.MemberRepository;
import com.example.itmonster.security.jwt.JwtDecoder;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequiredArgsConstructor
@Slf4j
@RestController
public class SseController {
    public static Map<Long, SseEmitter> sseEmitters = new ConcurrentHashMap<>();
    private final JwtDecoder jwtDecoder;
    private final MemberRepository memberRepository;

    @CrossOrigin("*")
    @GetMapping(value = "/sub", consumes = MediaType.ALL_VALUE)
    public SseEmitter subscribe(@RequestParam String token, HttpServletResponse response){

        String username = jwtDecoder.decodeUsername(token);
        Member member = memberRepository.findByEmail(username)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Long memberId = member.getId();

        // 현재 클라이언트를 위한 SseEmitter 생성
        SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE);
        try{
            // 연결
            response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");

            sseEmitter.send(SseEmitter.event().name("connect").data("SSE연결 성공!"));
        } catch (IOException e){
            e.printStackTrace();
        }

        // memberId를 key값으로 SseEmitter 저장
        sseEmitters.put(memberId, sseEmitter);

        sseEmitter.onCompletion(() -> sseEmitters.remove(memberId));
        sseEmitter.onTimeout(() -> sseEmitters.remove(memberId));
        sseEmitter.onError((e) -> sseEmitters.remove(memberId));

        return sseEmitter;
    }
}
