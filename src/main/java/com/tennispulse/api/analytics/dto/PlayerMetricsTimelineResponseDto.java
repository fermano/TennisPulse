package com.tennispulse.api.analytics.dto;

import com.tennispulse.domain.analytics.AnalyticsMetric;

import java.util.List;
import java.util.Map;

public record PlayerMetricsTimelineResponseDto(
        String playerId,
        TimelineRange range,
        List<PlayerMonthlyMetricsDto> timeline,
        Map<AnalyticsMetric, Double> overallAverages
) {}
