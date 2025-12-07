package com.tennispulse.service;

import com.tennispulse.domain.ClubEntity;
import com.tennispulse.domain.MatchEntity;
import com.tennispulse.domain.MatchStatus;
import com.tennispulse.domain.PlayerEntity;
import com.tennispulse.repository.ClubRepository;
import com.tennispulse.repository.MatchRepository;
import com.tennispulse.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final ClubRepository clubRepository;

    @Transactional
    public MatchEntity create(UUID clubId, UUID player1Id, UUID player2Id) {
        ClubEntity club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("Club not found: " + clubId));

        PlayerEntity p1 = playerRepository.findById(player1Id)
                .orElseThrow(() -> new IllegalArgumentException("Player1 not found: " + player1Id));
        PlayerEntity p2 = playerRepository.findById(player2Id)
                .orElseThrow(() -> new IllegalArgumentException("Player2 not found: " + player2Id));

        MatchEntity match = MatchEntity.builder()
                .club(club)
                .player1(p1)
                .player2(p2)
                .status(MatchStatus.SCHEDULED)
                .startTime(null)
                .endTime(null)
                .build();

        return matchRepository.save(match);
    }

    public List<MatchEntity> findAll() {
        return matchRepository.findAll();
    }

    public MatchEntity findById(UUID id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + id));
    }

    public MatchEntity updateStatus(UUID id, MatchStatus status, UUID winnerId, String finalScore) {
        MatchEntity match = findById(id);
        match.setStatus(status);

        if (status == MatchStatus.IN_PROGRESS && match.getStartTime() == null) {
            match.setStartTime(Instant.now());
        }

        if (status == MatchStatus.COMPLETED) {
            // winner and finalScore are REQUIRED here
            if (winnerId == null || finalScore == null || finalScore.isBlank()) {
                throw new IllegalArgumentException("Winner and finalScore are required when completing a match.");
            }

            PlayerEntity winner = playerRepository.findById(winnerId)
                    .orElseThrow(() -> new IllegalArgumentException("Winner not found: " + winnerId));

            match.setWinner(winner);
            match.setFinalScore(finalScore);
            match.setEndTime(Instant.now());
        }

        return matchRepository.save(match);
    }

    public void delete(UUID id) {
        matchRepository.deleteById(id);
    }
}
