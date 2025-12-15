package com.tennispulse.service;

import com.tennispulse.api.MatchController;
import com.tennispulse.domain.ClubEntity;
import com.tennispulse.domain.MatchEntity;
import com.tennispulse.domain.MatchStatus;
import com.tennispulse.domain.PlayerEntity;
import com.tennispulse.repository.ClubRepository;
import com.tennispulse.repository.MatchRepository;
import com.tennispulse.repository.PlayerRepository;
import com.tennispulse.service.analytics.SqsMatchEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchService {

    private final SqsMatchEventPublisher matchEventPublisher;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final ClubRepository clubRepository;

    @Transactional
    public MatchEntity create(String clubId, String player1Id, String player2Id) {
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

        MatchEntity saved = matchRepository.save(match);
        log.info("Match created: id={}, clubId={}, player1Id={}, player2Id={}, status={}",
                saved.getId(), clubId, player1Id, player2Id, saved.getStatus());
        return saved;
    }

    public List<MatchEntity> findAll() {
        return matchRepository.findAll();
    }

    public MatchEntity findById(String id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + id));
    }

    @Transactional
    public MatchEntity updateStatus(String id, MatchController.UpdateMatchStatusRequest updateMatchStatusRequest) {
        MatchEntity match = findById(id);
        MatchStatus oldStatus = match.getStatus();

        log.info("Updating match status: id={}, updateMatchStatusRequest={}",
                id, updateMatchStatusRequest);

        MatchStatus status = updateMatchStatusRequest.getStatus();
        match.setStatus(status);

        if (status == MatchStatus.IN_PROGRESS && match.getStartTime() == null) {
            match.setStartTime(Instant.now());
        }

        if (status == MatchStatus.COMPLETED) {
            String winnerId = updateMatchStatusRequest.getWinnerId();
            String finalScore = updateMatchStatusRequest.getFinalScore();
            if (winnerId == null || finalScore == null || finalScore.isBlank()) {
                throw new IllegalArgumentException("Winner and finalScore are required when completing a match.");
            }

            if (CollectionUtils.isEmpty(updateMatchStatusRequest.getPlayerStats())) {
                log.warn("Match {} completed without stats payload", id);
            } else {
                List<MatchController.PlayerStatsRequest> playerStatsRequests = updateMatchStatusRequest.getPlayerStats();
                matchEventPublisher.publishMatchCompleted(match, playerStatsRequests);
            }

            PlayerEntity winner = playerRepository.findById(winnerId)
                    .orElseThrow(() -> new IllegalArgumentException("Winner not found: " + winnerId));

            match.setWinner(winner);
            match.setFinalScore(finalScore);
            match.setEndTime(Instant.now());
        }

        if (status == MatchStatus.CANCELLED) {
            match.setWinner(null);
            match.setFinalScore(null);
            match.setEndTime(Instant.now());
        }

        MatchEntity saved = matchRepository.save(match);
        log.info("Match status updated: id={}, from={} to={}, winnerId={}, finalScore={}",
                id, oldStatus, saved.getStatus(),
                saved.getWinner() != null ? saved.getWinner().getId() : null,
                saved.getFinalScore());
        return saved;
    }

    public void delete(String id) {
        matchRepository.deleteById(id);
        log.info("Match deleted: id={}", id);
    }
}
