package com.tennispulse.api;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class PlayerPerformanceRankingDto {
    private UUID playerId;
    private String playerName;
    private double averageScore;
    private long matchesCount;
}
