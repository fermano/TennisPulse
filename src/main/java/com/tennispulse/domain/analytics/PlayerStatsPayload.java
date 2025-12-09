package com.tennispulse.domain.analytics;

import lombok.Data;

import java.util.UUID;

@Data
public class PlayerStatsPayload {
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
