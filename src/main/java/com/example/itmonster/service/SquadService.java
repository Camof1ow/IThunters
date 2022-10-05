package com.example.itmonster.service;

import com.example.itmonster.controller.response.SquadResponseDto;
import com.example.itmonster.domain.Channel;
import com.example.itmonster.domain.Member;
import com.example.itmonster.domain.MemberInChannel;
import com.example.itmonster.domain.Offer;
import com.example.itmonster.domain.Offer.ClassType;
import com.example.itmonster.domain.Quest;
import com.example.itmonster.domain.Squad;
import com.example.itmonster.exceptionHandler.CustomException;
import com.example.itmonster.exceptionHandler.ErrorCode;
import com.example.itmonster.repository.ChannelRepository;
import com.example.itmonster.repository.MemberInChannelRepository;
import com.example.itmonster.repository.OfferRepository;
import com.example.itmonster.repository.QuestRepository;
import com.example.itmonster.repository.SquadRepository;
import com.example.itmonster.socket.AlarmDto;
import com.example.itmonster.socket.AlarmService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SquadService {

    private final QuestRepository questRepository;
    private final OfferRepository offerRepository;
    private final SquadRepository squadRepository;
    private final MemberInChannelRepository memberInChannelRepository;
    private final ChannelRepository channelRepository;
    private final AlarmService alarmService;

    // 스쿼드에 멤버 추가
    @Transactional
    @CacheEvict(value = "chatSquadInfo", key = "#channelId")
    public boolean addSquadMember(Offer offer, Quest quest, Long channelId, Member member) {

        Member questOwner = offer.getQuest().getMember();
        if (!Objects.equals(questOwner.getId(), member.getId())) {
            throw new CustomException(ErrorCode.INVALID_AUTHORITY);   // 에러 : 게시글 주인이 아닌 사람이 접근할 경우.
        }

        Channel channel = channelRepository.findById(channelId)
            .orElseThrow(() -> new CustomException(ErrorCode.CHANNEL_NOT_FOUND));

        Member offeredMember = offer.getOfferedMember();
        ClassType classType = offer.getClassType();

        Squad squad = squadRepository.findAllByMemberAndQuest(offeredMember, quest).orElse(null);
        if (squad != null) {
            throw new CustomException(ErrorCode.SQUAD_CONFLICT);
        }

        // Squad DB에 추가
        squadRepository.save(
            Squad.builder()
                .quest(quest)
                .member(offeredMember)
                .build());

        AlarmDto alarmDto = AlarmDto.builder()
            .receiverId(offer.getOfferedMember().getId())
            .content("퀘스트 \""+offer.getQuest().getTitle()+"\"에 대한 합류요청이 승인되었습니다.")
            .build();

        alarmService.sendAlarm(alarmDto);

        // Offer DB 에서 삭제
        offerRepository.delete(offer);

        if (classType == ClassType.BACKEND) {
            quest.updateBackendCount(quest.getBackend() - 1);
            if( quest.getBackend() <= 0 ) offerRepository.deleteAllByQuestAndClassType( quest , ClassType.BACKEND );
        } else if (classType == ClassType.FRONTEND) {
            quest.updateFrontendCount(quest.getFrontend() - 1);
            if( quest.getFrontend() <= 0 ) offerRepository.deleteAllByQuestAndClassType( quest , ClassType.FRONTEND );
        } else if (classType == ClassType.FULLSTACK) {
            quest.updateFullstackCount(quest.getFullstack() - 1);
            if( quest.getFullstack() <= 0 ) offerRepository.deleteAllByQuestAndClassType( quest , ClassType.FULLSTACK );
        } else {
            quest.updateDesignerCount(quest.getDesigner() - 1);
            if( quest.getDesigner() <= 0 ) offerRepository.deleteAllByQuestAndClassType( quest , ClassType.DESIGNER );
        }

        questRepository.save(quest);
        quest.updateStatus(quest);

        memberInChannelRepository.save(MemberInChannel.builder()  // 대화방에 해당 유저 추가
            .channel(channel)
            .member(offeredMember)
            .build());

        return true;
    }

    // 퀘스트에 합류한 스쿼드멤버들 불러오기
    @Transactional(readOnly = true)
    public List<SquadResponseDto> getSquadMembersByQuest(Long questId) {

        Quest quest = questRepository.findById(questId).orElseThrow(
            () -> new CustomException(ErrorCode.QUEST_NOT_FOUND)  // 에러 : 퀘스트 존재하지 않음.
        );

        List<Squad> squadList = squadRepository.findAllByQuest(quest);
        List<SquadResponseDto> squadResponseDtos = new ArrayList<>();

        squadList.forEach(squad -> squadResponseDtos.add(new SquadResponseDto(squad)));

        return squadResponseDtos;

    }

    // 내가 소속된 스쿼드 보기
    @Transactional(readOnly = true)
    public List<SquadResponseDto> getMySquads(Member me) {

        List<Squad> squadList = squadRepository.findAllByMember(me);
        List<SquadResponseDto> squadResponseDtos = new ArrayList<>();

        squadList.forEach(squad -> squadResponseDtos.add(new SquadResponseDto(squad)));

        return squadResponseDtos;

    }

    // 스쿼드에서 멤버 삭제
    @Transactional
    @CacheEvict(value = "chatSquadInfo", key = "#channelId")
    public boolean deleteSquadMember(Long squadId, Member member) {

        Squad squad = squadRepository.findById(squadId).orElseThrow(
            () -> new CustomException(ErrorCode.SQUAD_MEMBER_NOT_FOUND)   // 에러 : 스쿼드에 멤버가 존재하지 않음.
        );

        Member questOwner = squad.getQuest().getMember();

        if (!Objects.equals(questOwner.getId(), member.getId()) && !Objects.equals(squad.getMember().getId(), member.getId())) {
            throw new CustomException(ErrorCode.INVALID_AUTHORITY);   // 본인이나 퀘스트리더만 탈퇴 가능
        }
        squadRepository.delete(squad);
        return true;
    }
}
