package com.tennispulse.domain;

import com.tennispulse.domain.analytics.PlayerStatsPayload;
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
}
