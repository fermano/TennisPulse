package com.tennispulse.domain.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MetricValue {
    private Double value;
    private MetricStatus status;
}
