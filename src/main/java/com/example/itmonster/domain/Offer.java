package com.example.itmonster.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table( indexes = {
    @Index( name ="createdAt", columnList = "createdAt"),
    @Index( name ="classType", columnList = "classType")
})
public class Offer extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Quest quest;

    @JoinColumn
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member offeredMember;

    @Column( nullable = false )
    private ClassType classType;

    @Column(nullable = false)
    private OfferType offerType;

    @Column
    private String content;

    public enum ClassType{
        FRONTEND, BACKEND, FULLSTACK, DESIGNER
    }

    public enum OfferType{
        NEWOFFER, ACCEPTED, DECLINED
    }
}
