package com.tennispulse.api.analytics.dto;

import java.util.List;
import java.util.Map;

public record PlayerMetricsTimelineResponseDto(
        String playerId,
        TimelineRange range,
        List<PlayerMonthlyMetricsDto> timeline,
        Map<String, Double> overallAverages
) {}
