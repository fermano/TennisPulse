package com.tennispulse.api;

import com.tennispulse.domain.MatchEntity;
import com.tennispulse.domain.MatchStatus;
import com.tennispulse.service.MatchQueryService;
import com.tennispulse.service.MatchService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    private final MatchQueryService matchQueryService;

    @PostMapping
    public ResponseEntity<MatchEntity> create(@RequestBody CreateMatchRequest request) {
        MatchEntity created = matchService.create(
                request.getClubId(),
                request.getPlayer1Id(),
                request.getPlayer2Id()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<MatchResponse> list() {
        return matchQueryService.listAll();
    }

    @GetMapping("/{id}")
    public MatchResponse get(@PathVariable UUID id) {
        return matchQueryService.getById(id);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<MatchResponse> updateStatus(
            @PathVariable UUID id,
            @RequestBody UpdateMatchStatusRequest request
    ) {
        matchService.updateStatus(
                id,
                request.getStatus(),
                request.getWinnerId(),
                request.getFinalScore()
        );

        MatchResponse response = matchQueryService.getById(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        matchService.delete(id);
    }

    @Data
    public static class CreateMatchRequest {
        private UUID clubId;
        private UUID player1Id;
        private UUID player2Id;
    }

    @Data
    public static class UpdateMatchStatusRequest {
        private MatchStatus status;
        private UUID winnerId;
        private String finalScore;
    }

    public record MatchResponse(
            UUID id,
            UUID clubId,
            String clubName,
            UUID player1Id,
            String player1Name,
            UUID player2Id,
            String player2Name,
            UUID winnerId,
            String winnerName,
            String finalScore,
            MatchStatus status,
            Instant startTime,
            Instant endTime
    ) {}
}
