package com.example.itmonster.socket;

import com.example.itmonster.domain.Channel;
import com.example.itmonster.domain.Message;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findTop100ByChannelIdOrderByCreatedAtDesc(Long roomId);
    Message findTopByChannelIdOrderByCreatedAtDesc(Long channelId);
    List<Message> findAllByChannelOrderByIdDesc(Channel channel);
    List<Message> findAllByChannelOrderByIdDesc(Channel channel, Pageable pageable);
    List<Message> findByIdLessThanAndChannelOrderByIdDesc(Long id, Channel channel, Pageable pageable);
}
