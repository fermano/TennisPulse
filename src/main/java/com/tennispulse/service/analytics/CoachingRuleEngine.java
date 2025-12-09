package com.tennispulse.service.analytics;

import com.tennispulse.domain.analytics.AnalyticsMetric;
import com.tennispulse.domain.analytics.PlayerMatchCoachingAnalysis;

import java.util.Map;
import java.util.UUID;

public interface CoachingRuleEngine {
    PlayerMatchCoachingAnalysis analyze(UUID matchId,
                                        UUID playerId,
                                        Map<AnalyticsMetric, Double> rawMetrics);
}
