package com.example.itmonster.service;

import com.example.itmonster.controller.response.ChannelResponseDto;
import com.example.itmonster.domain.Channel;
import com.example.itmonster.domain.Member;
import com.example.itmonster.domain.MemberInChannel;
import com.example.itmonster.domain.Message;
import com.example.itmonster.domain.Quest;
import com.example.itmonster.domain.Squad;
import com.example.itmonster.repository.ChannelRepository;
import com.example.itmonster.repository.MemberInChannelRepository;
import com.example.itmonster.repository.QuestRepository;
import com.example.itmonster.repository.SquadRepository;
import com.example.itmonster.security.UserDetailsImpl;
import com.example.itmonster.socket.MessageRepository;
import com.example.itmonster.socket.MessageResponseDto;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final MemberInChannelRepository memberInChannelRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String MESSAGE = "MESSAGE";
    private final MessageRepository messageRepository;

    @Transactional
    public Channel createChannel(Quest quest) {
        Channel channel = Channel.builder()
            .channelName(quest.getTitle())
            .quest(quest)
            .build();
        channelRepository.save(channel);
        return channel;
    }

    @Transactional(readOnly = true)
    public List<ChannelResponseDto> readChannel(UserDetailsImpl userDetails) {
        Member member = userDetails.getMember();
        List<MemberInChannel> memberInChannels = memberInChannelRepository.findAllByMember(member);

        HashOperations<String, String, List<MessageResponseDto>> opsHashChatMessage = redisTemplate.opsForHash();
        List<ChannelResponseDto> result = new ArrayList<>();
        for(MemberInChannel memberInChannel : memberInChannels){
            String lastMessage = "";
            List<MessageResponseDto> temp1 = (opsHashChatMessage.get(MESSAGE,
                String.valueOf(memberInChannel.getChannel().getId())));

            if(temp1 != null){
                temp1 = temp1.stream().sorted(Comparator.comparing(MessageResponseDto::getCreatedAt, Comparator.reverseOrder()))
                    .collect(Collectors.toList());
                lastMessage = temp1.get(0).getContent();
            }else{
                Message message = messageRepository.findTopByChannelIdOrderByCreatedAtDesc(memberInChannel.getChannel().getId());
                if(message != null){
                    lastMessage = message.getContent();
                }
            }
            result.add(ChannelResponseDto.builder()
                .id(memberInChannel.getChannel().getId())
                .channelName(memberInChannel.getChannel().getChannelName())
                .lastMessage(lastMessage)
                .build());
        }
        return result;
    }
}
