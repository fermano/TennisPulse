package com.tennispulse.service;

import com.tennispulse.api.MatchController;
import com.tennispulse.domain.MatchEntity;
import com.tennispulse.domain.PlayerEntity;
import com.tennispulse.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchQueryService {

    private final MatchRepository matchRepository;

    @Transactional(readOnly = true)
    public MatchController.MatchResponse getById(UUID id) {
        MatchEntity m = matchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + id));

        return toResponse(m);
    }

    @Transactional(readOnly = true)
    public List<MatchController.MatchResponse> listAll() {
        return matchRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private MatchController.MatchResponse toResponse(MatchEntity m) {
        PlayerEntity winner = m.getWinner();
        return new MatchController.MatchResponse(
                m.getId(),
                m.getClub() != null ? m.getClub().getId() : null,
                m.getClub() != null ? m.getClub().getName() : null,
                m.getPlayer1().getId(),
                m.getPlayer1().getName(),
                m.getPlayer2().getId(),
                m.getPlayer2().getName(),
                winner != null ? winner.getId() : null,
                winner != null ? winner.getName() : null,
                m.getFinalScore(),
                m.getStatus(),
                m.getStartTime(),
                m.getEndTime()
        );
    }
}