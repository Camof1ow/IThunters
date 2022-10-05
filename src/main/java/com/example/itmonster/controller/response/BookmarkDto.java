package com.example.itmonster.controller.response;

import com.example.itmonster.domain.Quest;
import lombok.Getter;

@Getter
public class BookmarkDto {
    private final Long questId;
    private final String questTitle;

    public BookmarkDto(Quest quest){
        this.questId = quest.getId();
        this.questTitle = quest.getTitle();
    }
}
