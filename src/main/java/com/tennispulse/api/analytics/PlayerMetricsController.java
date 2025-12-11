package com.tennispulse.api.analytics;

import com.tennispulse.api.analytics.dto.PlayerMetricsTimelineResponseDto;
import com.tennispulse.api.analytics.dto.TimelineRange;
import com.tennispulse.service.analytics.PlayerMetricsTimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/analytics/player")
@RequiredArgsConstructor
public class PlayerMetricsController {

    private final PlayerMetricsTimelineService timelineService;

    @GetMapping("/{playerId}/timeline")
    public PlayerMetricsTimelineResponseDto getTimeline(
            @PathVariable UUID playerId,
            @RequestParam(defaultValue = "ALL_TIME") TimelineRange range
    ) {
        return timelineService.getPlayerTimeline(playerId, range);
    }
}
