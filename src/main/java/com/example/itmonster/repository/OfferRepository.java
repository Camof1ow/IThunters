package com.example.itmonster.repository;

import com.example.itmonster.domain.Member;
import com.example.itmonster.domain.Offer;
import com.example.itmonster.domain.Offer.ClassType;
import com.example.itmonster.domain.Offer.OfferType;
import com.example.itmonster.domain.Quest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfferRepository extends JpaRepository<Offer, Long> {


    Optional<Offer> findByOfferedMemberAndQuest(Member offeredMember, Quest quest);

    List<Offer> findAllByOfferTypeAndQuestIn(OfferType offerType, List<Quest> quests);

    void deleteAllByQuestAndClassTypeAndOfferType(Quest quest, ClassType backend, OfferType offerType);

    List<Offer> findAllByQuest(Quest quest);

    List<Offer> findAllByOfferedMemberAndOfferType(Member member, OfferType offerType);
}
