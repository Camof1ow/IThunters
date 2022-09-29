package com.example.itmonster.repository;

import com.example.itmonster.domain.Channel;
import com.example.itmonster.domain.Member;
import com.example.itmonster.domain.MemberInChannel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberInChannelRepository extends JpaRepository<MemberInChannel, Long> {
    List<MemberInChannel> findAllByMember(Member member);
    List<MemberInChannel> findAllByChannel(Channel channel);
    void deleteByMemberAndChannel(Member member, Channel channel);

}
