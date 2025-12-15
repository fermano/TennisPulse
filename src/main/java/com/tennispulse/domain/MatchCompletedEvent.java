package com.tennispulse.domain;

import com.tennispulse.domain.analytics.PlayerStatsPayload;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class MatchCompletedEvent {
    private String matchId;
    private String winnerId;
    private String finalScore;
    private Instant createdAt;
    private List<PlayerStatsPayload> playerStats;
}
