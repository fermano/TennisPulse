package com.tennispulse.service.analytics;

import com.tennispulse.domain.analytics.AnalyticsMetric;
import com.tennispulse.domain.analytics.CoachingStatus;
import com.tennispulse.domain.analytics.CoachingTip;
import com.tennispulse.domain.analytics.MetricStatus;
import com.tennispulse.domain.analytics.MetricValue;
import com.tennispulse.domain.analytics.PlayerMatchCoachingAnalysis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ThresholdBasedCoachingRuleEngine implements CoachingRuleEngine {

    @Override
    public PlayerMatchCoachingAnalysis analyze(UUID matchId,
                                               UUID playerId,
                                               Map<AnalyticsMetric, Double> rawMetrics) {

        Map<AnalyticsMetric, MetricValue> classified = rawMetrics.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new MetricValue(e.getValue(), classifyMetric(e.getKey(), e.getValue()))
                ));

        CoachingStatus coachingStatus = deriveCoachingStatus(
                classified.values().stream()
                        .map(MetricValue::getStatus)
                        .toList()
        );

        List<CoachingTip> tips = new ArrayList<>();
        classified.forEach((metric, value) ->
                addTipsForMetric(tips, metric, value.getStatus())
        );

        return new PlayerMatchCoachingAnalysis(
                matchId,
                playerId,
                coachingStatus,
                classified,
                tips
        );
    }

    // ───────────────────────── classification logic ─────────────────────────

    private MetricStatus classifyMetric(AnalyticsMetric metric, double value) {
        return switch (metric) {
            case FIRST_SERVE_IN -> classifyPercentage(value, 50, 60, 70);
            // <50  = CRITICAL
            // 50–59 = WARNING
            // 60–69 = GOOD
            // >=70  = EXCELLENT

            case FIRST_SERVE_POINTS_WON -> classifyPercentage(value, 60, 65, 75);
            // <60   = CRITICAL
            // 60–64 = WARNING
            // 65–74 = GOOD
            // >=75  = EXCELLENT

            case SECOND_SERVE_POINTS_WON -> classifyPercentage(value, 40, 50, 60);
            // <40   = CRITICAL
            // 40–49 = WARNING
            // 50–59 = GOOD
            // >=60  = EXCELLENT

            case UNFORCED_ERRORS_FOREHAND,
                 UNFORCED_ERRORS_BACKHAND -> classifyErrors(value, 5, 10, 18);
            // 0–5   = EXCELLENT
            // 6–10  = GOOD
            // 11–18 = WARNING
            // >18   = CRITICAL

            case WINNERS -> classifyWinners(value, 8, 15, 25);
            // <8    = CRITICAL
            // 8–14  = WARNING
            // 15–24 = GOOD
            // >=25  = EXCELLENT

            case BREAK_POINT_CONVERSION -> classifyPercentage(value, 25, 40, 60);
            // <25   = CRITICAL
            // 25–39 = WARNING
            // 40–59 = GOOD
            // >=60  = EXCELLENT

            case BREAK_POINTS_SAVED -> classifyPercentage(value, 25, 45, 65);
            // <25   = CRITICAL
            // 25–44 = WARNING
            // 45–64 = GOOD
            // >=65  = EXCELLENT

            case NET_POINTS_WON -> classifyPercentage(value, 50, 60, 70);
            // <50   = CRITICAL
            // 50–59 = WARNING
            // 60–69 = GOOD
            // >=70  = EXCELLENT

            case LONG_RALLY_WIN_RATE -> classifyPercentage(value, 35, 45, 60);
            // <35   = CRITICAL
            // 35–44 = WARNING
            // 45–59 = GOOD
            // >=60  = EXCELLENT
        };
    }

    /**
     * Generic helper for "higher is better" percentage metrics.
     * thresholds: critical < t1 <= warning < t2 <= good < t3 <= excellent
     */
    private MetricStatus classifyPercentage(double value, double t1, double t2, double t3) {
        if (value >= t3) return MetricStatus.EXCELLENT;
        if (value >= t2) return MetricStatus.GOOD;
        if (value >= t1) return MetricStatus.WARNING;
        return MetricStatus.CRITICAL;
    }

    /**
     * Helper for error-count metrics (lower is better).
     * thresholds: excellent <= t1 < good <= t2 < warning <= t3 < critical
     */
    private MetricStatus classifyErrors(double value, int t1, int t2, int t3) {
        if (value <= t1) return MetricStatus.EXCELLENT;
        if (value <= t2) return MetricStatus.GOOD;
        if (value <= t3) return MetricStatus.WARNING;
        return MetricStatus.CRITICAL;
    }

    /**
     * Winners: depends on match length, but we use a simple tier model.
     *   <  t1  -> CRITICAL
     *   t1–(t2-1) -> WARNING
     *   t2–(t3-1) -> GOOD
     *   >= t3 -> EXCELLENT
     */
    private MetricStatus classifyWinners(double value, int t1, int t2, int t3) {
        if (value >= t3) return MetricStatus.EXCELLENT;
        if (value >= t2) return MetricStatus.GOOD;
        if (value >= t1) return MetricStatus.WARNING;
        return MetricStatus.CRITICAL;
    }

    // ───────────────────────── aggregation + tips (already had) ─────────────────────────

    private CoachingStatus deriveCoachingStatus(Collection<MetricStatus> statuses) {
        long critical = statuses.stream().filter(s -> s == MetricStatus.CRITICAL).count();
        long warning = statuses.stream().filter(s -> s == MetricStatus.WARNING).count();

        if (critical >= 2 || (critical == 1 && warning >= 2)) {
            return CoachingStatus.AT_RISK;
        }
        if (critical == 1 || warning >= 2) {
            return CoachingStatus.NEEDS_FOCUS;
        }
        return CoachingStatus.ON_TRACK;
    }

    private void addTipsForMetric(List<CoachingTip> tips,
                                  AnalyticsMetric metric,
                                  MetricStatus status) {
        if (status == MetricStatus.EXCELLENT) return;

        switch (metric) {
            case FIRST_SERVE_IN -> {
                if (status == MetricStatus.GOOD) {
                    tips.add(new CoachingTip(
                            "FIRST_SERVE_IN_GOOD",
                            "Your first serve % is solid. Push consistency to reach 70%+.",
                            metric
                    ));
                } else {
                    tips.add(new CoachingTip(
                            "FIRST_SERVE_IN_LOW",
                            "First serve % is low. Focus on safer targets and a smoother toss.",
                            metric
                    ));
                }
            }
            case FIRST_SERVE_POINTS_WON -> tips.add(new CoachingTip(
                    "FIRST_SERVE_POINTS_WON_LOW",
                    "You’re not winning enough points on first serve. Work on placement and the first shot after serve.",
                    metric
            ));
            case SECOND_SERVE_POINTS_WON -> tips.add(new CoachingTip(
                    "SECOND_SERVE_WEAK",
                    "Second serve points won is low. Practice spin/kick serves for more safety and depth.",
                    metric
            ));
            case UNFORCED_ERRORS_FOREHAND -> tips.add(new CoachingTip(
                    "FOREHAND_ERRORS_HIGH",
                    "Forehand unforced errors are high. Emphasize preparation and more margin over the net.",
                    metric
            ));
            case UNFORCED_ERRORS_BACKHAND -> tips.add(new CoachingTip(
                    "BACKHAND_ERRORS_HIGH",
                    "Backhand errors are high. Add crosscourt rally drills and footwork patterns.",
                    metric
            ));
            case WINNERS -> tips.add(new CoachingTip(
                    "WINNERS_LOW",
                    "Aggression level is low. Look to attack short balls and step inside the court more often.",
                    metric
            ));
            case BREAK_POINT_CONVERSION -> tips.add(new CoachingTip(
                    "BREAK_CONVERSION_LOW",
                    "Break point conversion is low. Go in with clear return patterns and avoid going passive on big points.",
                    metric
            ));
            case BREAK_POINTS_SAVED -> tips.add(new CoachingTip(
                    "BREAK_POINTS_SAVED_LOW",
                    "You struggle to save break points. Develop trusted serve patterns under pressure.",
                    metric
            ));
            case NET_POINTS_WON -> tips.add(new CoachingTip(
                    "NET_POINTS_WEAK",
                    "Net points won % is low. Work on approach shot quality and volley technique.",
                    metric
            ));
            case LONG_RALLY_WIN_RATE -> tips.add(new CoachingTip(
                    "LONG_RALLIES_WEAK",
                    "You’re losing long rallies. Add endurance-heavy rally drills and consistency work.",
                    metric
            ));
        }
    }
}
