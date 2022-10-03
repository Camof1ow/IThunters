package com.example.itmonster.notification;

import static com.example.itmonster.notification.SseController.sseEmitters;

import com.example.itmonster.domain.Offer;
import com.example.itmonster.domain.Quest;
import com.example.itmonster.exceptionHandler.CustomException;
import com.example.itmonster.exceptionHandler.ErrorCode;
import com.example.itmonster.repository.OfferRepository;
import com.example.itmonster.repository.QuestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequiredArgsConstructor
@Service
@Slf4j
public class NotificationService {

    private final QuestRepository questRepository;
    private final OfferRepository offerRepository;

    public void notifyNewOfferEvent(Long questId){

        Quest quest = questRepository.findById(questId)
            .orElseThrow(() -> new CustomException(ErrorCode.QUEST_NOT_FOUND));

        Long questOwnerId = quest.getMember().getId();

        if(sseEmitters.containsKey(questOwnerId)){
            SseEmitter sseEmitter = sseEmitters.get(questOwnerId);
            System.out.println(sseEmitter);
            try{
                sseEmitter.send(SseEmitter.event().name("offer")
                    .data("등록하신 퀘스트 \""+quest.getTitle()+"\"에 대한 새로운 합류요청이 도착했습니다."));
            }catch (Exception e){
                sseEmitters.remove(questOwnerId);
            }
        }
    }

    public void acceptOfferEvent(Long offerId){

        Offer offer = offerRepository.findById(offerId)
            .orElseThrow(() -> new CustomException(ErrorCode.OFFER_NOT_FOUND));

        Long offeredMemberId = offer.getOfferedMember().getId();

        if(sseEmitters.containsKey(offeredMemberId)){
            SseEmitter sseEmitter = sseEmitters.get(offeredMemberId);
            try{
                sseEmitter.send(SseEmitter.event().name("offer")
                    .data("퀘스트 \""+offer.getQuest().getTitle()+"\"에 대한 합류요청이 승인되었습니다."));
            }catch (Exception e){
                sseEmitters.remove(offeredMemberId);
            }
        }
    }

    public void declineOfferEvent(Long offerId){

        Offer offer = offerRepository.findById(offerId)
            .orElseThrow(() -> new CustomException(ErrorCode.OFFER_NOT_FOUND));

        Long offeredMemberId = offer.getOfferedMember().getId();

        if(sseEmitters.containsKey(offeredMemberId)){
            SseEmitter sseEmitter = sseEmitters.get(offeredMemberId);
            try{
                sseEmitter.send(SseEmitter.event().name("offer")
                    .data("퀘스트 \""+offer.getQuest().getTitle()+"\"에 대한 합류요청이 거절되었습니다."));
            }catch (Exception e){
                sseEmitters.remove(offeredMemberId);
            }
        }
    }
}
