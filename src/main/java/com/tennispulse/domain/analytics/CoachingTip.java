package com.tennispulse.domain.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CoachingTip {
    private String code;
    private String message;
    private AnalyticsMetric metric;
}
