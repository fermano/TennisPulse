package com.tennispulse.domain.analytics;

import lombok.Data;

@Data
public class PlayerStatsPayload {
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
