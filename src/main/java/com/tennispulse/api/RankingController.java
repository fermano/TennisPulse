package com.tennispulse.api;

import com.tennispulse.api.dto.PlayerPerformanceRankingDto;
import com.tennispulse.api.dto.PlayerWinsRankingDto;
import com.tennispulse.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/overall/all-time")
    public List<PlayerPerformanceRankingDto> overallAllTime(@RequestParam(defaultValue = "10") int limit) {
        return rankingService.getTopOverallAllTime(limit);
    }

    @GetMapping("/overall/current-year")
    public List<PlayerPerformanceRankingDto> overallCurrentYear(@RequestParam(defaultValue = "10") int limit) {
        return rankingService.getTopOverallCurrentYear(limit);
    }

    @GetMapping("/overall/current-month")
    public List<PlayerPerformanceRankingDto> overallCurrentMonth(@RequestParam(defaultValue = "10") int limit) {
        return rankingService.getTopOverallCurrentMonth(limit);
    }

    @GetMapping("/wins/current-year")
    public List<PlayerWinsRankingDto> winsCurrentYear(@RequestParam(defaultValue = "10") int limit) {
        return rankingService.getTopWinnersCurrentYear(limit);
    }

    @GetMapping("/wins/last-month")
    public List<PlayerWinsRankingDto> winsLastMonth(@RequestParam(defaultValue = "10") int limit) {
        return rankingService.getTopWinnersLastMonth(limit);
    }
}
