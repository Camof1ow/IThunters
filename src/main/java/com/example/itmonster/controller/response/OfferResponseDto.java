package com.example.itmonster.controller.response;

import com.example.itmonster.domain.Offer;
import com.example.itmonster.domain.Offer.ClassType;
import com.example.itmonster.domain.Offer.OfferType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OfferResponseDto {

    private Long offerId;

    private Long questId;

    private String questTitle;

    private Long offeredMemberId;

    private String offeredMemberNickname;

    private String profileImg;

    private ClassType classType;

    private LocalDateTime createdAt;

    private OfferType offerType;

    private String content;

    public OfferResponseDto ( Offer offer ){
        offerId = offer.getId();
        offeredMemberId = offer.getOfferedMember().getId();
        questId = offer.getQuest().getId();
        offeredMemberNickname = offer.getOfferedMember().getNickname();
        profileImg = offer.getOfferedMember().getProfileImg();
        questTitle = offer.getQuest().getTitle();
        classType = offer.getClassType();
        offerType = offer.getOfferType();
        content = offer.getContent();
        createdAt = offer.getCreatedAt();
    }
}
