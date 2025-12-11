package com.tennispulse.domain.analytics.dto;

import com.tennispulse.domain.analytics.AnalyticsMetric;
import com.tennispulse.domain.analytics.TimelineRange;

import java.util.List;
import java.util.Map;

public record PlayerMetricsTimelineResponseDto(
        String playerId,
        TimelineRange range,
        List<PlayerMonthlyMetricsDto> timeline,
        Map<AnalyticsMetric, Double> overallAverages
) {}
