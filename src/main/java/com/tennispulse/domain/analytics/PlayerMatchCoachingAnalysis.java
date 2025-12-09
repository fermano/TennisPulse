package com.tennispulse.domain.analytics;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class PlayerMatchCoachingAnalysis {

    private UUID matchId;
    private UUID playerId;
    private CoachingStatus coachingStatus;
    private Map<AnalyticsMetric, MetricValue> metrics;
    private List<CoachingTip> tips;
    private String engineVersion;
    private Instant createdAt;

    public PlayerMatchCoachingAnalysis(
            UUID matchId,
            UUID playerId,
            CoachingStatus coachingStatus,
            Map<AnalyticsMetric, MetricValue> metrics,
            List<CoachingTip> tips
    ) {
        this.matchId = matchId;
        this.playerId = playerId;
        this.coachingStatus = coachingStatus;
        this.metrics = metrics;
        this.tips = tips;
        this.engineVersion = "v1";
        this.createdAt = Instant.now();
    }
}
