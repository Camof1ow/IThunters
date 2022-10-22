package com.example.itmonster.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table( indexes = {
    @Index( name ="createdAt", columnList = "createdAt"),
    @Index( name ="title", columnList = "title"),
    @Index( name ="duration", columnList = "duration")
})
public class Quest extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "member_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    private String title;

    @Column(length = 1000)
    private String content;

    private Long frontend;
    private Long backend;
    private Long fullstack;
    private Long designer;
    private Long duration; // 주단위로 기간 설정

    //진행 유무만 확인
    private Boolean status; // 모집중 / 모집완료
    private Boolean isComplete; // 퀘스트 진행중 / 완료

    @OneToMany(mappedBy = "quest")
    private List<Comment> comments;

    @Formula("(select count(*) from bookmark where bookmark.quest_id=id)")
    private int bookmarkCnt;

    @Builder.Default
    @OneToMany(mappedBy = "quest")
    private List<StackOfQuest> stacks = new ArrayList<>();

    public void updateQuest(String title, String content, Long frontend,
        Long backend, Long fullstack, Long designer, Long duration) {
        this.title = title;
        this.content = content;
        this.frontend = frontend;
        this.backend = backend;
        this.fullstack = fullstack;
        this.designer = designer;
        this.duration = duration;
        this.status = frontend + backend + fullstack + designer == 0;
    }

    public void updateBackendCount(Long count) {
        this.backend = count;
    }

    public void updateFrontendCount(Long count) {
        this.frontend = count;
    }

    public void updateFullstackCount(Long count) {
        this.fullstack = count;
    }

    public void updateDesignerCount(Long count) {
        this.designer = count;
    }

    public void updateStatus(Quest quest) {
        this.status = quest.frontend + quest.backend + quest.fullstack + quest.designer == 0;
    }

    public void completeQuest(){
        this.isComplete = true;
    }

}
