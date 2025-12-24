package com.tennispulse.api.analytics.dto;

import java.time.YearMonth;
import java.util.Map;

public record PlayerMonthlyMetricsDto(
        YearMonth month,
        Map<String, Double> averages
) {}
