package com.tennispulse.service.analytics;

import com.tennispulse.api.analytics.dto.HighlightCategory;
import com.tennispulse.api.analytics.dto.HighlightsDashboardResponse;
import com.tennispulse.api.analytics.dto.PlayerHighlightDto;
import com.tennispulse.api.analytics.dto.TimelineRange;
import com.tennispulse.domain.PlayerEntity;
import com.tennispulse.domain.analytics.AnalyticsMetric;
import com.tennispulse.repository.PlayerRepository;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerHighlightsServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private AggregationResults<Document> aggregationResults;

    @InjectMocks
    private PlayerHighlightsService playerHighlightsService;

    private String player1Id;
    private String player2Id;
    private PlayerEntity player1;
    private PlayerEntity player2;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID().toString();
        player2Id = UUID.randomUUID().toString();

        player1 = PlayerEntity.builder()
                .id(player1Id)
                .name("Rafael Nadal")
                .build();

        player2 = PlayerEntity.builder()
                .id(player2Id)
                .name("Roger Federer")
                .build();
    }

    @Test
    void getHighlights_shouldReturnAllCategoriesWithBestPlayers_whenDataExists() {
        // Arrange
        Document doc1 = createPlayerDocument(
                player1Id,
                95.0,  // FIRST_SERVE_IN
                85.0,  // FIRST_SERVE_POINTS_WON
                75.0,  // SECOND_SERVE_POINTS_WON
                80.0,  // LONG_RALLY_WIN_RATE
                5.0,   // UNFORCED_ERRORS_FOREHAND
                3.0,   // UNFORCED_ERRORS_BACKHAND
                90.0,  // NET_POINTS_WON
                25.0,  // WINNERS
                70.0,  // BREAK_POINT_CONVERSION
                65.0   // BREAK_POINTS_SAVED
        );

        Document doc2 = createPlayerDocument(
                player2Id,
                88.0,  // FIRST_SERVE_IN
                80.0,  // FIRST_SERVE_POINTS_WON
                70.0,  // SECOND_SERVE_POINTS_WON
                75.0,  // LONG_RALLY_WIN_RATE
                8.0,   // UNFORCED_ERRORS_FOREHAND
                6.0,   // UNFORCED_ERRORS_BACKHAND
                85.0,  // NET_POINTS_WON
                20.0,  // WINNERS
                60.0,  // BREAK_POINT_CONVERSION
                55.0   // BREAK_POINTS_SAVED
        );

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("player_match_analytics"), eq(Document.class)))
                .thenReturn(aggregationResults);
        when(aggregationResults.getMappedResults()).thenReturn(Arrays.asList(doc1, doc2));
        when(playerRepository.findById(player1Id)).thenReturn(Optional.of(player1));
        when(playerRepository.findById(player2Id)).thenReturn(Optional.of(player2));

        // Act
        HighlightsDashboardResponse response = playerHighlightsService.getHighlights(TimelineRange.LAST_MONTH);

        // Assert
        assertNotNull(response);
        assertEquals(TimelineRange.LAST_MONTH, response.range());
        assertNotNull(response.highlights());
        assertEquals(5, response.highlights().size());

        // Verify BEST_SERVE highlight (player1 should win with higher serve stats)
        PlayerHighlightDto bestServe = response.highlights().get(HighlightCategory.BEST_SERVE);
        assertNotNull(bestServe);
        assertEquals(player1Id, bestServe.playerId());
        assertEquals("Rafael Nadal", bestServe.playerName());
        assertTrue(bestServe.score() > 0);
        assertNotNull(bestServe.details());
        assertTrue(bestServe.details().containsKey("FIRST_SERVE_IN"));

        // Verify BEST_NET_PLAYER highlight
        PlayerHighlightDto bestNet = response.highlights().get(HighlightCategory.BEST_NET_PLAYER);
        assertNotNull(bestNet);
        assertEquals(player1Id, bestNet.playerId());

        // Verify BEST_PRESSURE_PLAYER highlight
        PlayerHighlightDto bestPressure = response.highlights().get(HighlightCategory.BEST_PRESSURE_PLAYER);
        assertNotNull(bestPressure);
        assertEquals(player1Id, bestPressure.playerId());

        // Verify CLEANEST_BASELINE highlight (player1 has fewer errors)
        PlayerHighlightDto cleanestBaseline = response.highlights().get(HighlightCategory.CLEANEST_BASELINE);
        assertNotNull(cleanestBaseline);
        assertEquals(player1Id, cleanestBaseline.playerId());

        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("player_match_analytics"), eq(Document.class));
        verify(playerRepository, atLeastOnce()).findById(anyString());
    }

    @Test
    void getHighlights_shouldHandleAllTimeRange_withNullFromDate() {
        // Arrange
        Document doc = createPlayerDocument(
                player1Id, 90.0, 80.0, 70.0, 75.0, 5.0, 3.0, 85.0, 20.0, 65.0, 60.0
        );

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("player_match_analytics"), eq(Document.class)))
                .thenReturn(aggregationResults);
        when(aggregationResults.getMappedResults()).thenReturn(Collections.singletonList(doc));
        when(playerRepository.findById(player1Id)).thenReturn(Optional.of(player1));

        // Act
        HighlightsDashboardResponse response = playerHighlightsService.getHighlights(TimelineRange.ALL_TIME);

        // Assert
        assertNotNull(response);
        assertEquals(TimelineRange.ALL_TIME, response.range());
        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("player_match_analytics"), eq(Document.class));
    }

    @Test
    void getHighlights_shouldReturnEmptyHighlights_whenNoPlayersFound() {
        // Arrange
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("player_match_analytics"), eq(Document.class)))
                .thenReturn(aggregationResults);
        when(aggregationResults.getMappedResults()).thenReturn(Collections.emptyList());

        // Act
        HighlightsDashboardResponse response = playerHighlightsService.getHighlights(TimelineRange.LAST_6_MONTHS);

        // Assert
        assertNotNull(response);
        assertEquals(TimelineRange.LAST_6_MONTHS, response.range());
        assertNotNull(response.highlights());
        assertTrue(response.highlights().isEmpty());
    }

    @Test
    void getHighlights_shouldUseUnknownPlayerName_whenPlayerNotFoundInRepository() {
        // Arrange
        Document doc = createPlayerDocument(
                player1Id, 90.0, 80.0, 70.0, 75.0, 5.0, 3.0, 85.0, 20.0, 65.0, 60.0
        );

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("player_match_analytics"), eq(Document.class)))
                .thenReturn(aggregationResults);
        when(aggregationResults.getMappedResults()).thenReturn(Collections.singletonList(doc));
        when(playerRepository.findById(player1Id)).thenReturn(Optional.empty());

        // Act
        HighlightsDashboardResponse response = playerHighlightsService.getHighlights(TimelineRange.YEAR_TO_DATE);

        // Assert
        assertNotNull(response);
        assertFalse(response.highlights().isEmpty());

        // All highlights should have "Unknown Player" since the player is not found
        response.highlights().values().forEach(highlight -> {
            assertEquals("Unknown Player", highlight.playerName());
            assertEquals(player1Id, highlight.playerId());
        });

        // Verify it was called 5 times (once per highlight category)
        verify(playerRepository, times(5)).findById(player1Id);
    }

    @Test
    void getHighlights_shouldHandleMissingMetrics_withDefaultZeroValues() {
        // Arrange
        Document doc = new Document("playerId", player1Id);
        // Only add a few metrics, leaving others null
        doc.put(AnalyticsMetric.FIRST_SERVE_IN.name(), 85.0);
        doc.put(AnalyticsMetric.NET_POINTS_WON.name(), 75.0);
        // All other metrics will be null/missing

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("player_match_analytics"), eq(Document.class)))
                .thenReturn(aggregationResults);
        when(aggregationResults.getMappedResults()).thenReturn(Collections.singletonList(doc));
        when(playerRepository.findById(player1Id)).thenReturn(Optional.of(player1));

        // Act
        HighlightsDashboardResponse response = playerHighlightsService.getHighlights(TimelineRange.LAST_MONTH);

        // Assert
        assertNotNull(response);
        assertFalse(response.highlights().isEmpty());

        // Service should handle missing metrics gracefully with zero defaults
        PlayerHighlightDto bestServe = response.highlights().get(HighlightCategory.BEST_SERVE);
        assertNotNull(bestServe);
        assertTrue(bestServe.score() >= 0);
    }

    @Test
    void getHighlights_shouldCalculateCorrectScores_forBestServe() {
        // Arrange
        Document doc = createPlayerDocument(
                player1Id, 100.0, 90.0, 80.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0
        );

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("player_match_analytics"), eq(Document.class)))
                .thenReturn(aggregationResults);
        when(aggregationResults.getMappedResults()).thenReturn(Collections.singletonList(doc));
        when(playerRepository.findById(player1Id)).thenReturn(Optional.of(player1));

        // Act
        HighlightsDashboardResponse response = playerHighlightsService.getHighlights(TimelineRange.LAST_MONTH);

        // Assert
        PlayerHighlightDto bestServe = response.highlights().get(HighlightCategory.BEST_SERVE);
        assertNotNull(bestServe);

        // Expected score: 0.4 * 100 + 0.3 * 90 + 0.3 * 80 = 40 + 27 + 24 = 91
        assertEquals(91.0, bestServe.score(), 0.01);
        assertEquals(100.0, bestServe.details().get("FIRST_SERVE_IN"));
        assertEquals(90.0, bestServe.details().get("FIRST_SERVE_POINTS_WON"));
        assertEquals(80.0, bestServe.details().get("SECOND_SERVE_POINTS_WON"));
    }

    @Test
    void getHighlights_shouldHandleYearToDateRange() {
        // Arrange
        Document doc = createPlayerDocument(
                player1Id, 90.0, 80.0, 70.0, 75.0, 5.0, 3.0, 85.0, 20.0, 65.0, 60.0
        );

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("player_match_analytics"), eq(Document.class)))
                .thenReturn(aggregationResults);
        when(aggregationResults.getMappedResults()).thenReturn(Collections.singletonList(doc));
        when(playerRepository.findById(player1Id)).thenReturn(Optional.of(player1));

        // Act
        HighlightsDashboardResponse response = playerHighlightsService.getHighlights(TimelineRange.YEAR_TO_DATE);

        // Assert
        assertNotNull(response);
        assertEquals(TimelineRange.YEAR_TO_DATE, response.range());
        assertFalse(response.highlights().isEmpty());
    }

    // Helper method to create a complete player document with all metrics
    private Document createPlayerDocument(String playerId,
                                          double firstServeIn,
                                          double firstServeWon,
                                          double secondServeWon,
                                          double longRallyWin,
                                          double unforcedErrorsFh,
                                          double unforcedErrorsBh,
                                          double netPointsWon,
                                          double winners,
                                          double breakPointConv,
                                          double breakPointsSaved) {
        Document doc = new Document("playerId", playerId);
        doc.put(AnalyticsMetric.FIRST_SERVE_IN.name(), firstServeIn);
        doc.put(AnalyticsMetric.FIRST_SERVE_POINTS_WON.name(), firstServeWon);
        doc.put(AnalyticsMetric.SECOND_SERVE_POINTS_WON.name(), secondServeWon);
        doc.put(AnalyticsMetric.LONG_RALLY_WIN_RATE.name(), longRallyWin);
        doc.put(AnalyticsMetric.UNFORCED_ERRORS_FOREHAND.name(), unforcedErrorsFh);
        doc.put(AnalyticsMetric.UNFORCED_ERRORS_BACKHAND.name(), unforcedErrorsBh);
        doc.put(AnalyticsMetric.NET_POINTS_WON.name(), netPointsWon);
        doc.put(AnalyticsMetric.WINNERS.name(), winners);
        doc.put(AnalyticsMetric.BREAK_POINT_CONVERSION.name(), breakPointConv);
        doc.put(AnalyticsMetric.BREAK_POINTS_SAVED.name(), breakPointsSaved);
        return doc;
    }
}