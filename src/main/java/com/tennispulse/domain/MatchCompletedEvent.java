package com.tennispulse.domain;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class MatchCompletedEvent {

    private UUID matchId;
    private UUID winnerId;
    private String finalScore;
    private Instant occurredAt;

    private List<PlayerStatsPayload> playerStats;

    @Data
    public static class PlayerStatsPayload {
        private UUID playerId;

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
}
