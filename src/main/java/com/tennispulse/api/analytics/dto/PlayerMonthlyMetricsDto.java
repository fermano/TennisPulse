package com.tennispulse.api.analytics.dto;

import com.tennispulse.domain.analytics.AnalyticsMetric;

import java.time.YearMonth;
import java.util.Map;

public record PlayerMonthlyMetricsDto(
        YearMonth month,
        Map<AnalyticsMetric, Double> averages
) {}
