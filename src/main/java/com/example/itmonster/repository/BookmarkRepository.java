package com.example.itmonster.repository;

import com.example.itmonster.domain.Bookmark;
import com.example.itmonster.domain.Member;
import com.example.itmonster.domain.Quest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Long countAllByQuest(Quest quest);
    boolean existsByMarkedMemberAndQuest(Member member, Quest quest);
    void deleteByMarkedMemberAndQuest(Member member, Quest quest);
    List<Bookmark> findAllByMarkedMember(Member member);

}
