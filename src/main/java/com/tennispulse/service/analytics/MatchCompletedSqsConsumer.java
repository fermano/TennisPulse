package com.tennispulse.service.analytics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennispulse.domain.MatchCompletedEvent;
import com.tennispulse.domain.analytics.AnalyticsMetric;
import com.tennispulse.domain.analytics.PlayerMatchAnalyticsDocument;
import com.tennispulse.domain.analytics.PlayerMatchCoachingAnalysis;
import com.tennispulse.domain.analytics.PlayerStatsPayload;
import com.tennispulse.repository.analytics.PlayerMatchAnalyticsRepository;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchCompletedSqsConsumer {

    private final ObjectMapper objectMapper;
    private final CoachingRuleEngine coachingRuleEngine;
    private final PlayerMatchAnalyticsRepository analyticsRepository;

    @SqsListener("${tennispulse.sqs.match-completed-queue-name}")
    public void handleMessage(@Payload String messageBody) {
        try {
            MatchCompletedEvent event =
                    objectMapper.readValue(messageBody, MatchCompletedEvent.class);

            processEvent(event);
        } catch (Exception e) {
            log.error("Failed to process message from match-completed queue. Body: {}", messageBody, e);

        }
    }

    private void processEvent(MatchCompletedEvent event) {
        if (event.getPlayerStats() == null || event.getPlayerStats().isEmpty()) {
            log.warn("MatchCompletedEvent {} without playerStats – skipping analytics", event.getMatchId());
            return;
        }

        event.getPlayerStats().forEach(statsPayload -> {
            Map<AnalyticsMetric, Double> rawMetrics = mapToRawMetrics(statsPayload);

            PlayerMatchCoachingAnalysis analysis =
                    coachingRuleEngine.analyze(event.getMatchId(), statsPayload.getPlayerId(), rawMetrics);

            saveAnalysis(event, statsPayload, analysis);

            log.info("Stored analytics for match {} player {}", event.getMatchId(), statsPayload.getPlayerId());
        });
    }

    private void saveAnalysis(MatchCompletedEvent event, PlayerStatsPayload statsPayload, PlayerMatchCoachingAnalysis analysis) {
        PlayerMatchAnalyticsDocument doc =
                PlayerMatchAnalyticsDocument.from(event, statsPayload, analysis);

        analyticsRepository.save(doc);
    }

    private Map<AnalyticsMetric, Double> mapToRawMetrics(PlayerStatsPayload s) {
        return Map.of(
                AnalyticsMetric.FIRST_SERVE_IN,            s.getFirstServeIn(),
                AnalyticsMetric.FIRST_SERVE_POINTS_WON,    s.getFirstServePointsWon(),
                AnalyticsMetric.SECOND_SERVE_POINTS_WON,   s.getSecondServePointsWon(),
                AnalyticsMetric.UNFORCED_ERRORS_FOREHAND,  s.getUnforcedErrorsForehand().doubleValue(),
                AnalyticsMetric.UNFORCED_ERRORS_BACKHAND,  s.getUnforcedErrorsBackhand().doubleValue(),
                AnalyticsMetric.WINNERS,                   s.getWinners().doubleValue(),
                AnalyticsMetric.BREAK_POINT_CONVERSION,    s.getBreakPointConversion(),
                AnalyticsMetric.BREAK_POINTS_SAVED,        s.getBreakPointsSaved(),
                AnalyticsMetric.NET_POINTS_WON,            s.getNetPointsWon(),
                AnalyticsMetric.LONG_RALLY_WIN_RATE,       s.getLongRallyWinRate()
        );
    }
}
