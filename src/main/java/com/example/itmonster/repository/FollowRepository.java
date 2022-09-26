package com.example.itmonster.repository;

import com.example.itmonster.domain.Follow;
import com.example.itmonster.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;


public interface FollowRepository extends JpaRepository<Follow, Long> {

    Follow findByFollowingIdAndMeId (Long following, Long Me);

    Boolean existsByFollowingAndMe (Member following,Member me);
}
