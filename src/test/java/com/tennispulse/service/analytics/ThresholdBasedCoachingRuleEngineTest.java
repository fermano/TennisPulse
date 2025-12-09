package com.tennispulse.service.analytics;

import com.tennispulse.domain.analytics.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ThresholdBasedCoachingRuleEngineTest {

    private final ThresholdBasedCoachingRuleEngine engine = new ThresholdBasedCoachingRuleEngine();

    @Test
    void analyze_firstServeIn_boundaries_shouldClassifyCorrectly() {
        UUID matchId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        // < 50 => CRITICAL
        var resultCritical = engine.analyze(
                matchId,
                playerId,
                Map.of(AnalyticsMetric.FIRST_SERVE_IN, 49.0)
        );
        assertEquals(
                MetricStatus.CRITICAL,
                resultCritical.getMetrics().get(AnalyticsMetric.FIRST_SERVE_IN).getStatus()
        );

        // 50–59 => WARNING
        var resultWarning = engine.analyze(
                matchId,
                playerId,
                Map.of(AnalyticsMetric.FIRST_SERVE_IN, 55.0)
        );
        assertEquals(
                MetricStatus.WARNING,
                resultWarning.getMetrics().get(AnalyticsMetric.FIRST_SERVE_IN).getStatus()
        );

        // 60–69 => GOOD
        var resultGood = engine.analyze(
                matchId,
                playerId,
                Map.of(AnalyticsMetric.FIRST_SERVE_IN, 65.0)
        );
        assertEquals(
                MetricStatus.GOOD,
                resultGood.getMetrics().get(AnalyticsMetric.FIRST_SERVE_IN).getStatus()
        );

        // >= 70 => EXCELLENT
        var resultExcellent = engine.analyze(
                matchId,
                playerId,
                Map.of(AnalyticsMetric.FIRST_SERVE_IN, 72.0)
        );
        assertEquals(
                MetricStatus.EXCELLENT,
                resultExcellent.getMetrics().get(AnalyticsMetric.FIRST_SERVE_IN).getStatus()
        );
    }

    @Test
    void analyze_twoCriticalMetrics_shouldReturnAtRisk() {
        UUID matchId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        // FIRST_SERVE_IN < 50 => CRITICAL
        // SECOND_SERVE_POINTS_WON < 40 => CRITICAL
        Map<AnalyticsMetric, Double> raw = Map.of(
                AnalyticsMetric.FIRST_SERVE_IN, 45.0,
                AnalyticsMetric.SECOND_SERVE_POINTS_WON, 30.0
        );

        PlayerMatchCoachingAnalysis analysis = engine.analyze(matchId, playerId, raw);

        assertEquals(CoachingStatus.AT_RISK, analysis.getCoachingStatus());

        // Both metrics should be classified as CRITICAL
        assertEquals(MetricStatus.CRITICAL,
                analysis.getMetrics().get(AnalyticsMetric.FIRST_SERVE_IN).getStatus());
        assertEquals(MetricStatus.CRITICAL,
                analysis.getMetrics().get(AnalyticsMetric.SECOND_SERVE_POINTS_WON).getStatus());

        // Tips should contain something for both metrics
        List<CoachingTip> tips = analysis.getTips();
        assertTrue(
                tips.stream().anyMatch(t -> t.getMetric() == AnalyticsMetric.FIRST_SERVE_IN),
                "Expected a tip for FIRST_SERVE_IN"
        );
        assertTrue(
                tips.stream().anyMatch(t -> t.getMetric() == AnalyticsMetric.SECOND_SERVE_POINTS_WON),
                "Expected a tip for SECOND_SERVE_POINTS_WON"
        );
    }

    @Test
    void analyze_warningOnly_shouldReturnNeedsFocus() {
        UUID matchId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        // FIRST_SERVE_IN in WARNING zone (50–59)
        // BREAK_POINT_CONVERSION also WARNING (25–39)
        Map<AnalyticsMetric, Double> raw = Map.of(
                AnalyticsMetric.FIRST_SERVE_IN, 55.0,
                AnalyticsMetric.BREAK_POINT_CONVERSION, 30.0
        );

        PlayerMatchCoachingAnalysis analysis = engine.analyze(matchId, playerId, raw);

        assertEquals(CoachingStatus.NEEDS_FOCUS, analysis.getCoachingStatus());

        assertEquals(
                MetricStatus.WARNING,
                analysis.getMetrics().get(AnalyticsMetric.FIRST_SERVE_IN).getStatus()
        );
        assertEquals(
                MetricStatus.WARNING,
                analysis.getMetrics().get(AnalyticsMetric.BREAK_POINT_CONVERSION).getStatus()
        );
    }

    @Test
    void analyze_goodProfile_shouldReturnOnTrackAndFewTips() {
        UUID matchId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        Map<AnalyticsMetric, Double> raw = Map.of(
                AnalyticsMetric.FIRST_SERVE_IN, 68.0,               // GOOD
                AnalyticsMetric.FIRST_SERVE_POINTS_WON, 78.0,       // EXCELLENT
                AnalyticsMetric.UNFORCED_ERRORS_FOREHAND, 4.0,      // EXCELLENT
                AnalyticsMetric.UNFORCED_ERRORS_BACKHAND, 6.0       // GOOD
        );

        PlayerMatchCoachingAnalysis analysis = engine.analyze(matchId, playerId, raw);

        assertEquals(CoachingStatus.ON_TRACK, analysis.getCoachingStatus());

        // EXCELLENT metrics shouldn't generate tips
        assertTrue(
                analysis.getTips().stream()
                        .noneMatch(t -> t.getMetric() == AnalyticsMetric.FIRST_SERVE_POINTS_WON),
                "No tips expected for EXCELLENT metrics"
        );

        // But non-EXCELLENT metrics may produce tips
        boolean hasSomeTips = !analysis.getTips().isEmpty();
        assertTrue(hasSomeTips, "Expected at least one tip for non-excellent metrics");
    }
}
