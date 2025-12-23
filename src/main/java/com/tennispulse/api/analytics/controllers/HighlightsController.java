package com.tennispulse.api.analytics.controllers;

import com.tennispulse.api.analytics.dto.TimelineRange;
import com.tennispulse.api.analytics.dto.HighlightsDashboardResponse;
import com.tennispulse.service.analytics.PlayerHighlightsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics/highlights")
@RequiredArgsConstructor
public class HighlightsController {

    private final PlayerHighlightsService playerHighlightsService;

    @GetMapping("/{range}")
    public HighlightsDashboardResponse getHighlights(@PathVariable TimelineRange range) {
        return playerHighlightsService.getHighlights(range);
    }
}
