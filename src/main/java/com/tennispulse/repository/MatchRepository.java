package com.tennispulse.repository;

import com.tennispulse.domain.MatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<MatchEntity, String> {

    List<MatchEntity> findByPlayer1IdOrPlayer2Id(String player1Id, String player2Id);

    List<MatchEntity> findByClubId(String clubId);

    @Query("""
        select m.winner.id, m.winner.name, count(m)
        from MatchEntity m
        where m.status = com.tennispulse.domain.MatchStatus.COMPLETED
          and m.winner is not null
          and m.endTime between :from and :to
        group by m.winner.id, m.winner.name
        order by count(m) desc
    """)
    List<Object[]> findWinCountsBetween(Instant from, Instant to);
}