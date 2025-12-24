package com.tennispulse.api.analytics.controllers;

import com.tennispulse.api.analytics.dto.PlayerMetricsTimelineResponseDto;
import com.tennispulse.api.analytics.dto.PlayerMonthlyMetricsDto;
import com.tennispulse.api.analytics.dto.TimelineRange;
import com.tennispulse.domain.analytics.AnalyticsMetric;
import com.tennispulse.service.analytics.PlayerMetricsTimelineService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.YearMonth;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(PlayerMetricsController.class)
class PlayerMetricsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlayerMetricsTimelineService timelineService;

    @Test
    void getTimeline_shouldReturnTimeline_withDefaultAllTimeRange() throws Exception {
        // Arrange
        UUID playerId = UUID.randomUUID();

        Map<String, Double> metrics1 = new HashMap<>();
        metrics1.put(AnalyticsMetric.FIRST_SERVE_IN.name(), 85.0);
        metrics1.put(AnalyticsMetric.NET_POINTS_WON.name(), 78.0);

        Map<String, Double> metrics2 = new HashMap<>();
        metrics2.put(AnalyticsMetric.FIRST_SERVE_IN.name(), 82.0);
        metrics2.put(AnalyticsMetric.NET_POINTS_WON.name(), 75.0);

        List<PlayerMonthlyMetricsDto> timeline = Arrays.asList(
                new PlayerMonthlyMetricsDto(YearMonth.of(2024, 12), metrics1),
                new PlayerMonthlyMetricsDto(YearMonth.of(2024, 11), metrics2)
        );

        Map<String, Double> overallAverages = new HashMap<>();
        overallAverages.put(AnalyticsMetric.FIRST_SERVE_IN.name(), 83.5);
        overallAverages.put(AnalyticsMetric.NET_POINTS_WON.name(), 76.5);

        PlayerMetricsTimelineResponseDto response = new PlayerMetricsTimelineResponseDto(
                playerId.toString(),
                TimelineRange.ALL_TIME,
                timeline,
                overallAverages
        );

        when(timelineService.getPlayerTimeline(playerId, TimelineRange.ALL_TIME)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/analytics/player/{playerId}/timeline", playerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId", is(playerId.toString())))
                .andExpect(jsonPath("$.range", is("ALL_TIME")))
                .andExpect(jsonPath("$.timeline", hasSize(2)))
                .andExpect(jsonPath("$.timeline[0].month", is("2024-12")))
                .andExpect(jsonPath("$.timeline[0].averages.FIRST_SERVE_IN", is(85.0)))
                .andExpect(jsonPath("$.timeline[0].averages.NET_POINTS_WON", is(78.0)))
                .andExpect(jsonPath("$.timeline[1].month", is("2024-11")))
                .andExpect(jsonPath("$.overallAverages.FIRST_SERVE_IN", is(83.5)))
                .andExpect(jsonPath("$.overallAverages.NET_POINTS_WON", is(76.5)));

        verify(timelineService).getPlayerTimeline(playerId, TimelineRange.ALL_TIME);
    }

    @Test
    void getTimeline_shouldReturnTimeline_withLastMonthRange() throws Exception {
        // Arrange
        UUID playerId = UUID.randomUUID();

        Map<String, Double> metrics = new HashMap<>();
        metrics.put(AnalyticsMetric.FIRST_SERVE_IN.name(), 88.0);

        List<PlayerMonthlyMetricsDto> timeline = Collections.singletonList(
                new PlayerMonthlyMetricsDto(YearMonth.of(2024, 12), metrics)
        );

        Map<String, Double> overallAverages = new HashMap<>();
        overallAverages.put(AnalyticsMetric.FIRST_SERVE_IN.name(), 88.0);

        PlayerMetricsTimelineResponseDto response = new PlayerMetricsTimelineResponseDto(
                playerId.toString(),
                TimelineRange.LAST_MONTH,
                timeline,
                overallAverages
        );

        when(timelineService.getPlayerTimeline(playerId, TimelineRange.LAST_MONTH)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/analytics/player/{playerId}/timeline", playerId)
                        .param("range", "LAST_MONTH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId", is(playerId.toString())))
                .andExpect(jsonPath("$.range", is("LAST_MONTH")))
                .andExpect(jsonPath("$.timeline", hasSize(1)))
                .andExpect(jsonPath("$.timeline[0].averages.FIRST_SERVE_IN", is(88.0)));

        verify(timelineService).getPlayerTimeline(playerId, TimelineRange.LAST_MONTH);
    }

    @Test
    void getTimeline_shouldReturnTimeline_withLast6MonthsRange() throws Exception {
        // Arrange
        UUID playerId = UUID.randomUUID();

        List<PlayerMonthlyMetricsDto> timeline = Collections.emptyList();
        Map<String, Double> overallAverages = new HashMap<>();

        PlayerMetricsTimelineResponseDto response = new PlayerMetricsTimelineResponseDto(
                playerId.toString(),
                TimelineRange.LAST_6_MONTHS,
                timeline,
                overallAverages
        );

        when(timelineService.getPlayerTimeline(playerId, TimelineRange.LAST_6_MONTHS)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/analytics/player/{playerId}/timeline", playerId)
                        .param("range", "LAST_6_MONTHS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId", is(playerId.toString())))
                .andExpect(jsonPath("$.range", is("LAST_6_MONTHS")))
                .andExpect(jsonPath("$.timeline", hasSize(0)));

        verify(timelineService).getPlayerTimeline(playerId, TimelineRange.LAST_6_MONTHS);
    }

    @Test
    void getTimeline_shouldReturnTimeline_withLast12MonthsRange() throws Exception {
        // Arrange
        UUID playerId = UUID.randomUUID();

        Map<String, Double> metrics = new HashMap<>();
        metrics.put(AnalyticsMetric.BREAK_POINT_CONVERSION.name(), 72.5);

        List<PlayerMonthlyMetricsDto> timeline = Collections.singletonList(
                new PlayerMonthlyMetricsDto(YearMonth.of(2024, 12), metrics)
        );

        Map<String, Double> overallAverages = new HashMap<>();
        overallAverages.put(AnalyticsMetric.BREAK_POINT_CONVERSION.name(), 72.5);

        PlayerMetricsTimelineResponseDto response = new PlayerMetricsTimelineResponseDto(
                playerId.toString(),
                TimelineRange.LAST_12_MONTHS,
                timeline,
                overallAverages
        );

        when(timelineService.getPlayerTimeline(playerId, TimelineRange.LAST_12_MONTHS)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/analytics/player/{playerId}/timeline", playerId)
                        .param("range", "LAST_12_MONTHS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId", is(playerId.toString())))
                .andExpect(jsonPath("$.range", is("LAST_12_MONTHS")))
                .andExpect(jsonPath("$.timeline[0].averages.BREAK_POINT_CONVERSION", is(72.5)));

        verify(timelineService).getPlayerTimeline(playerId, TimelineRange.LAST_12_MONTHS);
    }

    @Test
    void getTimeline_shouldReturnTimeline_withYearToDateRange() throws Exception {
        // Arrange
        UUID playerId = UUID.randomUUID();

        Map<String, Double> metrics = new HashMap<>();
        metrics.put(AnalyticsMetric.WINNERS.name(), 25.0);

        List<PlayerMonthlyMetricsDto> timeline = Collections.singletonList(
                new PlayerMonthlyMetricsDto(YearMonth.of(2024, 12), metrics)
        );

        Map<String, Double> overallAverages = new HashMap<>();
        overallAverages.put(AnalyticsMetric.WINNERS.name(), 25.0);

        PlayerMetricsTimelineResponseDto response = new PlayerMetricsTimelineResponseDto(
                playerId.toString(),
                TimelineRange.YEAR_TO_DATE,
                timeline,
                overallAverages
        );

        when(timelineService.getPlayerTimeline(playerId, TimelineRange.YEAR_TO_DATE)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/analytics/player/{playerId}/timeline", playerId)
                        .param("range", "YEAR_TO_DATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId", is(playerId.toString())))
                .andExpect(jsonPath("$.range", is("YEAR_TO_DATE")))
                .andExpect(jsonPath("$.timeline[0].averages.WINNERS", is(25.0)));

        verify(timelineService).getPlayerTimeline(playerId, TimelineRange.YEAR_TO_DATE);
    }

    @Test
    void getTimeline_shouldReturnEmptyTimeline_whenNoDataExists() throws Exception {
        // Arrange
        UUID playerId = UUID.randomUUID();

        List<PlayerMonthlyMetricsDto> emptyTimeline = Collections.emptyList();
        Map<String, Double> emptyAverages = new HashMap<>();

        PlayerMetricsTimelineResponseDto response = new PlayerMetricsTimelineResponseDto(
                playerId.toString(),
                TimelineRange.ALL_TIME,
                emptyTimeline,
                emptyAverages
        );

        when(timelineService.getPlayerTimeline(playerId, TimelineRange.ALL_TIME)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/analytics/player/{playerId}/timeline", playerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId", is(playerId.toString())))
                .andExpect(jsonPath("$.range", is("ALL_TIME")))
                .andExpect(jsonPath("$.timeline", hasSize(0)))
                .andExpect(jsonPath("$.overallAverages").isEmpty());

        verify(timelineService).getPlayerTimeline(playerId, TimelineRange.ALL_TIME);
    }

    @Test
    void getTimeline_shouldHandleMultipleMonths_withDifferentMetrics() throws Exception {
        // Arrange
        UUID playerId = UUID.randomUUID();

        Map<String, Double> metrics1 = new HashMap<>();
        metrics1.put(AnalyticsMetric.FIRST_SERVE_IN.name(), 85.0);
        metrics1.put(AnalyticsMetric.WINNERS.name(), 20.0);

        Map<String, Double> metrics2 = new HashMap<>();
        metrics2.put(AnalyticsMetric.FIRST_SERVE_IN.name(), 82.0);
        // WINNERS missing in this month

        Map<String, Double> metrics3 = new HashMap<>();
        metrics3.put(AnalyticsMetric.WINNERS.name(), 22.0);
        // FIRST_SERVE_IN missing in this month

        List<PlayerMonthlyMetricsDto> timeline = Arrays.asList(
                new PlayerMonthlyMetricsDto(YearMonth.of(2024, 12), metrics1),
                new PlayerMonthlyMetricsDto(YearMonth.of(2024, 11), metrics2),
                new PlayerMonthlyMetricsDto(YearMonth.of(2024, 10), metrics3)
        );

        Map<String, Double> overallAverages = new HashMap<>();
        overallAverages.put(AnalyticsMetric.FIRST_SERVE_IN.name(), 83.5);
        overallAverages.put(AnalyticsMetric.WINNERS.name(), 21.0);

        PlayerMetricsTimelineResponseDto response = new PlayerMetricsTimelineResponseDto(
                playerId.toString(),
                TimelineRange.ALL_TIME,
                timeline,
                overallAverages
        );

        when(timelineService.getPlayerTimeline(playerId, TimelineRange.ALL_TIME)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/analytics/player/{playerId}/timeline", playerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timeline", hasSize(3)))
                .andExpect(jsonPath("$.timeline[0].averages.FIRST_SERVE_IN", is(85.0)))
                .andExpect(jsonPath("$.timeline[0].averages.WINNERS", is(20.0)))
                .andExpect(jsonPath("$.timeline[1].averages.FIRST_SERVE_IN", is(82.0)))
                .andExpect(jsonPath("$.timeline[1].averages.WINNERS").doesNotExist())
                .andExpect(jsonPath("$.timeline[2].averages.FIRST_SERVE_IN").doesNotExist())
                .andExpect(jsonPath("$.timeline[2].averages.WINNERS", is(22.0)));

        verify(timelineService).getPlayerTimeline(playerId, TimelineRange.ALL_TIME);
    }

    @Test
    void getTimeline_shouldReturn400_forInvalidTimelineRange() throws Exception {
        // Arrange
        UUID playerId = UUID.randomUUID();

        // Act & Assert
        mockMvc.perform(get("/api/analytics/player/{playerId}/timeline", playerId)
                        .param("range", "INVALID_RANGE"))
                .andExpect(status().isBadRequest());

        verify(timelineService, never()).getPlayerTimeline(any(UUID.class), any(TimelineRange.class));
    }

    @Test
    void getTimeline_shouldReturn400_forInvalidUUID() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/analytics/player/{playerId}/timeline", "not-a-uuid"))
                .andExpect(status().isBadRequest());

        verify(timelineService, never()).getPlayerTimeline(any(UUID.class), any(TimelineRange.class));
    }

    @Test
    void getTimeline_shouldHandleAllMetrics() throws Exception {
        // Arrange
        UUID playerId = UUID.randomUUID();

        Map<String, Double> metrics = new HashMap<>();
        for (AnalyticsMetric metric : AnalyticsMetric.values()) {
            metrics.put(metric.name(), 75.0 + metric.ordinal());
        }

        List<PlayerMonthlyMetricsDto> timeline = Collections.singletonList(
                new PlayerMonthlyMetricsDto(YearMonth.of(2024, 12), metrics)
        );

        PlayerMetricsTimelineResponseDto response = new PlayerMetricsTimelineResponseDto(
                playerId.toString(),
                TimelineRange.ALL_TIME,
                timeline,
                metrics
        );

        when(timelineService.getPlayerTimeline(playerId, TimelineRange.ALL_TIME)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/analytics/player/{playerId}/timeline", playerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timeline[0].averages").isNotEmpty())
                .andExpect(jsonPath("$.overallAverages").isNotEmpty());

        verify(timelineService).getPlayerTimeline(playerId, TimelineRange.ALL_TIME);
    }

    @Test
    void getTimeline_shouldHandleChronologicalOrder() throws Exception {
        // Arrange
        UUID playerId = UUID.randomUUID();

        Map<String, Double> metrics1 = new HashMap<>();
        metrics1.put(AnalyticsMetric.FIRST_SERVE_IN.name(), 90.0);

        Map<String, Double> metrics2 = new HashMap<>();
        metrics2.put(AnalyticsMetric.FIRST_SERVE_IN.name(), 85.0);

        Map<String, Double> metrics3 = new HashMap<>();
        metrics3.put(AnalyticsMetric.FIRST_SERVE_IN.name(), 80.0);

        List<PlayerMonthlyMetricsDto> timeline = Arrays.asList(
                new PlayerMonthlyMetricsDto(YearMonth.of(2024, 12), metrics1),
                new PlayerMonthlyMetricsDto(YearMonth.of(2024, 11), metrics2),
                new PlayerMonthlyMetricsDto(YearMonth.of(2024, 10), metrics3)
        );

        Map<String, Double> overallAverages = new HashMap<>();
        overallAverages.put(AnalyticsMetric.FIRST_SERVE_IN.name(), 85.0);

        PlayerMetricsTimelineResponseDto response = new PlayerMetricsTimelineResponseDto(
                playerId.toString(),
                TimelineRange.LAST_6_MONTHS,
                timeline,
                overallAverages
        );

        when(timelineService.getPlayerTimeline(playerId, TimelineRange.LAST_6_MONTHS)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/analytics/player/{playerId}/timeline", playerId)
                        .param("range", "LAST_6_MONTHS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timeline", hasSize(3)))
                .andExpect(jsonPath("$.timeline[0].month", is("2024-12")))
                .andExpect(jsonPath("$.timeline[1].month", is("2024-11")))
                .andExpect(jsonPath("$.timeline[2].month", is("2024-10")))
                .andExpect(jsonPath("$.timeline[0].averages.FIRST_SERVE_IN", is(90.0)))
                .andExpect(jsonPath("$.timeline[1].averages.FIRST_SERVE_IN", is(85.0)))
                .andExpect(jsonPath("$.timeline[2].averages.FIRST_SERVE_IN", is(80.0)));

        verify(timelineService).getPlayerTimeline(playerId, TimelineRange.LAST_6_MONTHS);
    }
}