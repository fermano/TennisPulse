package com.tennispulse.repository;

import com.tennispulse.domain.MatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<MatchEntity, UUID> {

    List<MatchEntity> findByPlayer1IdOrPlayer2Id(UUID player1Id, UUID player2Id);

    List<MatchEntity> findByClubId(UUID clubId);
}