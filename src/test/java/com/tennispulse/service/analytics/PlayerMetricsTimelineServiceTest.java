package com.tennispulse.service.analytics;

import com.tennispulse.api.analytics.dto.PlayerMetricsTimelineResponseDto;
import com.tennispulse.api.analytics.dto.PlayerMonthlyMetricsDto;
import com.tennispulse.api.analytics.dto.TimelineRange;
import com.tennispulse.domain.analytics.AnalyticsMetric;
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

import java.time.YearMonth;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerMetricsTimelineServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private AggregationResults<Document> aggregationResults;

    @InjectMocks
    private PlayerMetricsTimelineService playerMetricsTimelineService;

    private UUID playerId;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
    }

    @Test
    void getPlayerTimeline_shouldReturnTimelineWithMonthlyMetrics_whenDataExists() {
        // Arrange
        Document doc1 = createMonthlyDocument(2024, 12, 85.0, 75.0, 65.0, 80.0);
        Document doc2 = createMonthlyDocument(2024, 11, 80.0, 70.0, 60.0, 75.0);
        Document doc3 = createMonthlyDocument(2024, 10, 78.0, 68.0, 58.0, 72.0);

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("player_match_analytics"), eq(Document.class)))
                .thenReturn(aggregationResults);
        when(aggregationResults.getMappedResults()).thenReturn(Arrays.asList(doc1, doc2, doc3));

        // Act
        PlayerMetricsTimelineResponseDto response = playerMetricsTimelineService.getPlayerTimeline(
                playerId,
                TimelineRange.LAST_6_MONTHS
        );

        // Assert
        assertNotNull(response);
        assertEquals(playerId.toString(), response.playerId());
        assertEquals(TimelineRange.LAST_6_MONTHS, response.range());
        assertNotNull(response.timeline());
        assertEquals(3, response.timeline().size());

        // Verify timeline is sorted descending (newest first)
        PlayerMonthlyMetricsDto first = response.timeline().getFirst();
        assertEquals(YearMonth.of(2024, 12), first.month());
        assertTrue(first.averages().containsKey(AnalyticsMetric.FIRST_SERVE_IN.name()));
        assertEquals(85.0, first.averages().get(AnalyticsMetric.FIRST_SERVE_IN.name()));

        PlayerMonthlyMetricsDto last = response.timeline().get(2);
        assertEquals(YearMonth.of(2024, 10), last.month());

        // Verify overall averages are computed
        assertNotNull(response.overallAverages());
        assertFalse(response.overallAverages().isEmpty());

        // Check FIRST_SERVE_IN average: (85 + 80 + 78) / 3 = 81.0
        assertEquals(81.0, response.overallAverages().get(AnalyticsMetric.FIRST_SERVE_IN.name()), 0.01);

        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("player_match_analytics"), eq(Document.class));
    }

    @Test
    void getPlayerTimeline_shouldHandleAllTimeRange_withNullFromDate() {
        // Arrange
        Document doc = createMonthlyDocument(2024, 12, 85.0, 75.0, 65.0, 80.0);

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("player_match_analytics"), eq(Document.class)))
                .thenReturn(aggregationResults);
        when(aggregationResults.getMappedResults()).thenReturn(Collections.singletonList(doc));

        // Act
        PlayerMetricsTimelineResponseDto response = playerMetricsTimelineService.getPlayerTimeline(
                playerId,
                TimelineRange.ALL_TIME
        );

        // Assert
        assertNotNull(response);
        assertEquals(TimelineRange.ALL_TIME, response.range());
        assertEquals(1, response.timeline().size());
        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("player_match_analytics"), eq(Document.class));
    }

    @Test
    void getPlayerTimeline_shouldReturnEmptyTimeline_whenNoDataExists() {
        // Arrange
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("player_match_analytics"), eq(Document.class)))
                .thenReturn(aggregationResults);
        when(aggregationResults.getMappedResults()).thenReturn(Collections.emptyList());

        // Act
        PlayerMetricsTimelineResponseDto response = playerMetricsTimelineService.getPlayerTimeline(
                playerId,
                TimelineRange.LAST_MONTH
        );

        // Assert
        assertNotNull(response);
        assertEquals(playerId.toString(), response.playerId());
        assertEquals(TimelineRange.LAST_MONTH, response.range());
        assertNotNull(response.timeline());
        assertTrue(response.timeline().isEmpty());
        assertNotNull(response.overallAverages());
        assertTrue(response.overallAverages().isEmpty());
    }

    @Test
    void getPlayerTimeline_shouldSkipDocuments_whenYearOrMonthIsMissing() {
        // Arrange
        Document validDoc = createMonthlyDocument(2024, 12, 85.0, 75.0, 65.0, 80.0);

        // Invalid documents missing year/month
        Document invalidDoc1 = new Document();
        invalidDoc1.put("month", 11);
        // Missing year

        Document invalidDoc2 = new Document();
        invalidDoc2.put("year", 2024);
        // Missing month

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("player_match_analytics"), eq(Document.class)))
                .thenReturn(aggregationResults);
        when(aggregationResults.getMappedResults()).thenReturn(Arrays.asList(validDoc, invalidDoc1, invalidDoc2));

        // Act
        PlayerMetricsTimelineResponseDto response = playerMetricsTimelineService.getPlayerTimeline(
                playerId,
                TimelineRange.LAST_MONTH
        );

        // Assert
        assertNotNull(response);
        assertEquals(1, response.timeline().size()); // Only valid doc should be included
        assertEquals(YearMonth.of(2024, 12), response.timeline().getFirst().month());
    }

    @Test
    void getPlayerTimeline_shouldHandleMissingMetrics_withDefaultValues() {
        // Arrange
        Document doc = new Document();
        doc.put("year", 2024);
        doc.put("month", 12);
        // Only add one metric
        doc.put(AnalyticsMetric.FIRST_SERVE_IN.name(), 85.0);
        // All other metrics are null/missing

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("player_match_analytics"), eq(Document.class)))
                .thenReturn(aggregationResults);
        when(aggregationResults.getMappedResults()).thenReturn(Collections.singletonList(doc));

        // Act
        PlayerMetricsTimelineResponseDto response = playerMetricsTimelineService.getPlayerTimeline(
                playerId,
                TimelineRange.LAST_MONTH
        );

        // Assert
        assertNotNull(response);
        assertEquals(1, response.timeline().size());

        PlayerMonthlyMetricsDto monthly = response.timeline().getFirst();
        assertEquals(1, monthly.averages().size()); // Only FIRST_SERVE_IN should be present
        assertTrue(monthly.averages().containsKey(AnalyticsMetric.FIRST_SERVE_IN.name()));
        assertEquals(85.0, monthly.averages().get(AnalyticsMetric.FIRST_SERVE_IN.name()));
    }

    @Test
    void getPlayerTimeline_shouldCalculateCorrectOverallAverages_acrossMultipleMonths() {
        // Arrange
        Document doc1 = createMonthlyDocument(2024, 12, 90.0, 80.0, 70.0, 85.0);
        Document doc2 = createMonthlyDocument(2024, 11, 80.0, 70.0, 60.0, 75.0);
        Document doc3 = createMonthlyDocument(2024, 10, 70.0, 60.0, 50.0, 65.0);

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("player_match_analytics"), eq(Document.class)))
                .thenReturn(aggregationResults);
        when(aggregationResults.getMappedResults()).thenReturn(Arrays.asList(doc1, doc2, doc3));

        // Act
        PlayerMetricsTimelineResponseDto response = playerMetricsTimelineService.getPlayerTimeline(
                playerId,
                TimelineRange.LAST_6_MONTHS
        );

        // Assert
        Map<String, Double> overallAverages = response.overallAverages();
        assertNotNull(overallAverages);

        // FIRST_SERVE_IN: (90 + 80 + 70) / 3 = 80.0
        assertEquals(80.0, overallAverages.get(AnalyticsMetric.FIRST_SERVE_IN.name()), 0.01);

        // FIRST_SERVE_POINTS_WON: (80 + 70 + 60) / 3 = 70.0
        assertEquals(70.0, overallAverages.get(AnalyticsMetric.FIRST_SERVE_POINTS_WON.name()), 0.01);

        // SECOND_SERVE_POINTS_WON: (70 + 60 + 50) / 3 = 60.0
        assertEquals(60.0, overallAverages.get(AnalyticsMetric.SECOND_SERVE_POINTS_WON.name()), 0.01);

        // NET_POINTS_WON: (85 + 75 + 65) / 3 = 75.0
        assertEquals(75.0, overallAverages.get(AnalyticsMetric.NET_POINTS_WON.name()), 0.01);
    }

    @Test
    void getPlayerTimeline_shouldHandleYearToDateRange() {
        // Arrange
        Document doc = createMonthlyDocument(2024, 12, 85.0, 75.0, 65.0, 80.0);

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("player_match_analytics"), eq(Document.class)))
                .thenReturn(aggregationResults);
        when(aggregationResults.getMappedResults()).thenReturn(Collections.singletonList(doc));

        // Act
        PlayerMetricsTimelineResponseDto response = playerMetricsTimelineService.getPlayerTimeline(
                playerId,
                TimelineRange.YEAR_TO_DATE
        );

        // Assert
        assertNotNull(response);
        assertEquals(TimelineRange.YEAR_TO_DATE, response.range());
        assertEquals(1, response.timeline().size());
    }

    @Test
    void getPlayerTimeline_shouldHandleLast12MonthsRange() {
        // Arrange
        Document doc = createMonthlyDocument(2024, 12, 85.0, 75.0, 65.0, 80.0);

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("player_match_analytics"), eq(Document.class)))
                .thenReturn(aggregationResults);
        when(aggregationResults.getMappedResults()).thenReturn(Collections.singletonList(doc));

        // Act
        PlayerMetricsTimelineResponseDto response = playerMetricsTimelineService.getPlayerTimeline(
                playerId,
                TimelineRange.LAST_12_MONTHS
        );

        // Assert
        assertNotNull(response);
        assertEquals(TimelineRange.LAST_12_MONTHS, response.range());
        assertEquals(1, response.timeline().size());
    }

    @Test
    void getPlayerTimeline_shouldHandleSingleMonthData() {
        // Arrange
        Document doc = createMonthlyDocument(2024, 12, 85.0, 75.0, 65.0, 80.0);

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("player_match_analytics"), eq(Document.class)))
                .thenReturn(aggregationResults);
        when(aggregationResults.getMappedResults()).thenReturn(Collections.singletonList(doc));

        // Act
        PlayerMetricsTimelineResponseDto response = playerMetricsTimelineService.getPlayerTimeline(
                playerId,
                TimelineRange.LAST_MONTH
        );

        // Assert
        assertNotNull(response);
        assertEquals(1, response.timeline().size());

        // Overall averages should equal the single month's values
        PlayerMonthlyMetricsDto monthly = response.timeline().getFirst();
        Map<String, Double> overallAverages = response.overallAverages();

        assertEquals(
                monthly.averages().get(AnalyticsMetric.FIRST_SERVE_IN.name()),
                overallAverages.get(AnalyticsMetric.FIRST_SERVE_IN.name())
        );
    }

    @Test
    void getPlayerTimeline_shouldHandlePartialMetrics_inDifferentMonths() {
        // Arrange
        Document doc1 = new Document();
        doc1.put("year", 2024);
        doc1.put("month", 12);
        doc1.put(AnalyticsMetric.FIRST_SERVE_IN.name(), 90.0);
        doc1.put(AnalyticsMetric.NET_POINTS_WON.name(), 85.0);

        Document doc2 = new Document();
        doc2.put("year", 2024);
        doc2.put("month", 11);
        doc2.put(AnalyticsMetric.FIRST_SERVE_IN.name(), 80.0);
        // NET_POINTS_WON is missing in this month

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("player_match_analytics"), eq(Document.class)))
                .thenReturn(aggregationResults);
        when(aggregationResults.getMappedResults()).thenReturn(Arrays.asList(doc1, doc2));

        // Act
        PlayerMetricsTimelineResponseDto response = playerMetricsTimelineService.getPlayerTimeline(
                playerId,
                TimelineRange.LAST_6_MONTHS
        );

        // Assert
        Map<String, Double> overallAverages = response.overallAverages();

        // FIRST_SERVE_IN should average across both months: (90 + 80) / 2 = 85.0
        assertEquals(85.0, overallAverages.get(AnalyticsMetric.FIRST_SERVE_IN.name()), 0.01);

        // NET_POINTS_WON should only use the one month that has it: 85.0 / 1 = 85.0
        assertEquals(85.0, overallAverages.get(AnalyticsMetric.NET_POINTS_WON.name()), 0.01);
    }

    // Helper method to create a monthly document with sample metrics
    private Document createMonthlyDocument(int year,
                                           int month,
                                           double firstServeIn,
                                           double firstServeWon,
                                           double secondServeWon,
                                           double netPointsWon) {
        Document doc = new Document();
        doc.put("year", year);
        doc.put("month", month);
        doc.put(AnalyticsMetric.FIRST_SERVE_IN.name(), firstServeIn);
        doc.put(AnalyticsMetric.FIRST_SERVE_POINTS_WON.name(), firstServeWon);
        doc.put(AnalyticsMetric.SECOND_SERVE_POINTS_WON.name(), secondServeWon);
        doc.put(AnalyticsMetric.NET_POINTS_WON.name(), netPointsWon);
        return doc;
    }
}