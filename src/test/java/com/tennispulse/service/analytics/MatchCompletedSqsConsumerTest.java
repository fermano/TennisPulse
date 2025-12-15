package com.tennispulse.service.analytics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennispulse.domain.MatchCompletedEvent;
import com.tennispulse.domain.analytics.AnalyticsMetric;
import com.tennispulse.domain.analytics.CoachingStatus;
import com.tennispulse.domain.analytics.CoachingTip;
import com.tennispulse.domain.analytics.MetricStatus;
import com.tennispulse.domain.analytics.MetricValue;
import com.tennispulse.domain.analytics.PlayerMatchCoachingAnalysis;
import com.tennispulse.domain.analytics.PlayerStatsPayload;
import com.tennispulse.repository.analytics.PlayerMatchAnalyticsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchCompletedSqsConsumerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CoachingRuleEngine coachingRuleEngine;

    @Mock
    private PlayerMatchAnalyticsRepository analyticsRepository;

    @InjectMocks
    private MatchCompletedSqsConsumer consumer;

    @Test
    void handleMessage_happyPath_shouldAnalyzeAndPersistOneDocumentPerPlayer() throws Exception {
        String matchId = UUID.randomUUID().toString();
        String player1Id = UUID.randomUUID().toString();
        String player2Id = UUID.randomUUID().toString();

        // Build two player stats payloads
        PlayerStatsPayload stats1 = new PlayerStatsPayload();
        stats1.setPlayerId(player1Id);
        stats1.setFirstServeIn(65.0);
        stats1.setFirstServePointsWon(72.0);
        stats1.setSecondServePointsWon(50.0);
        stats1.setUnforcedErrorsForehand(5);
        stats1.setUnforcedErrorsBackhand(7);
        stats1.setWinners(20);
        stats1.setBreakPointConversion(40.0);
        stats1.setBreakPointsSaved(55.0);
        stats1.setNetPointsWon(60.0);
        stats1.setLongRallyWinRate(48.0);

        PlayerStatsPayload stats2 = new PlayerStatsPayload();
        stats2.setPlayerId(player2Id);
        stats2.setFirstServeIn(58.0);
        stats2.setFirstServePointsWon(68.0);
        stats2.setSecondServePointsWon(45.0);
        stats2.setUnforcedErrorsForehand(8);
        stats2.setUnforcedErrorsBackhand(10);
        stats2.setWinners(15);
        stats2.setBreakPointConversion(35.0);
        stats2.setBreakPointsSaved(50.0);
        stats2.setNetPointsWon(55.0);
        stats2.setLongRallyWinRate(42.0);

        MatchCompletedEvent event = new MatchCompletedEvent();
        event.setMatchId(matchId);
        event.setWinnerId(player1Id);
        event.setFinalScore("6-4 6-3");
        event.setOccurredAt(Instant.now());
        event.setPlayerStats(List.of(stats1, stats2));

        // ObjectMapper should deserialize the message body into our event
        when(objectMapper.readValue(anyString(), eq(MatchCompletedEvent.class)))
                .thenReturn(event);

        // Stub rule engine (we don't care about precise content here, just that it's called)
        PlayerMatchCoachingAnalysis analysis1 =
                new PlayerMatchCoachingAnalysis(
                        matchId,
                        player1Id,
                        CoachingStatus.NEEDS_FOCUS,
                        Map.of(AnalyticsMetric.FIRST_SERVE_IN, new MetricValue(65.0, MetricStatus.GOOD)),
                        List.of(new CoachingTip("TIP1", "msg1", AnalyticsMetric.FIRST_SERVE_IN))
                );

        PlayerMatchCoachingAnalysis analysis2 =
                new PlayerMatchCoachingAnalysis(
                        matchId,
                        player2Id,
                        CoachingStatus.ON_TRACK,
                        Map.of(AnalyticsMetric.FIRST_SERVE_IN, new MetricValue(58.0, MetricStatus.WARNING)),
                        List.of(new CoachingTip("TIP2", "msg2", AnalyticsMetric.FIRST_SERVE_IN))
                );

        when(coachingRuleEngine.analyze(eq(matchId), eq(player1Id), anyMap()))
                .thenReturn(analysis1);
        when(coachingRuleEngine.analyze(eq(matchId), eq(player2Id), anyMap()))
                .thenReturn(analysis2);

        // when
        consumer.handleMessage("{\"dummy\":\"json\"}");

        // then
        verify(objectMapper).readValue(anyString(), eq(MatchCompletedEvent.class));

        // One analysis per player
        verify(coachingRuleEngine, times(1))
                .analyze(eq(matchId), eq(player1Id), anyMap());
        verify(coachingRuleEngine, times(1))
                .analyze(eq(matchId), eq(player2Id), anyMap());

        // One Mongo save per player
        verify(analyticsRepository, times(2)).save(any());
    }

    @Test
    void handleMessage_whenDeserializationFails_shouldNotAnalyzeOrPersist() throws Exception {
        // ObjectMapper fails to deserialize
        when(objectMapper.readValue(anyString(), eq(MatchCompletedEvent.class)))
                .thenThrow(new JsonProcessingException("boom") {});

        consumer.handleMessage("invalid-json");

        // No analysis or persistence should happen
        verifyNoInteractions(coachingRuleEngine);
        verifyNoInteractions(analyticsRepository);
    }

    @Test
    void handleMessage_whenEventHasNoPlayerStats_shouldSkipAnalytics() throws Exception {
        String matchId = UUID.randomUUID().toString();

        MatchCompletedEvent event = new MatchCompletedEvent();
        event.setMatchId(matchId);
        event.setPlayerStats(Collections.emptyList());

        when(objectMapper.readValue(anyString(), eq(MatchCompletedEvent.class)))
                .thenReturn(event);

        consumer.handleMessage("{\"dummy\":\"json\"}");

        // No calls to rule engine or Mongo when there are no stats
        verifyNoInteractions(coachingRuleEngine);
        verifyNoInteractions(analyticsRepository);
    }
}
