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
    public MatchResponse get(@PathVariable String id) {
        return matchQueryService.getById(id);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<MatchResponse> updateStatus(
            @PathVariable String id,
            @RequestBody UpdateMatchStatusRequest request
    ) {
        matchService.updateStatus(id, request);

        MatchResponse response = matchQueryService.getById(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        matchService.delete(id);
    }

    @Data
    public static class CreateMatchRequest {
        private String clubId;
        private String player1Id;
        private String player2Id;
    }

    @Data
    public static class UpdateMatchStatusRequest {
        private MatchStatus status;
        private String winnerId;
        private String finalScore;
        private List<PlayerStatsRequest> playerStats;
    }

    @Data
    public static class PlayerStatsRequest {
        private String playerId;
        private Double firstServeIn;
        private Double firstServePointsWon;
        private Double secondServePointsWon;
        private Integer unforcedErrorsForehand;
        private Integer unforcedErrorsBackhand;
        private Integer winners;
        private Double breakPointConversion;
        private Double breakPointsSaved;
        private Double netPointsWon;
        private Double longRallyWinRate;
    }

    public record MatchResponse(
            String id,
            String clubId,
            String clubName,
            String player1Id,
            String player1Name,
            String player2Id,
            String player2Name,
            String winnerId,
            String winnerName,
            String finalScore,
            MatchStatus status,
            Instant startTime,
            Instant endTime,
            Instant createdAt,
            Instant updatedAt
    ) {}
}
