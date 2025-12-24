package com.tennispulse.api.analytics.dto;

import java.util.Map;

public record HighlightsDashboardResponse(TimelineRange range, Map<String, PlayerHighlightDto> highlights) {
}
