package com.tennispulse.api.analytics.dto;

import java.util.Map;

public record PlayerHighlightDto(String playerId, String playerName, double score, Map<String, Double> details) {
}
