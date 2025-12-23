package com.tennispulse.api.controllers;

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

    @GetMapping("/wins/current-year")
    public List<PlayerWinsRankingDto> winsCurrentYear(@RequestParam(defaultValue = "10") int limit) {
        return rankingService.getTopWinnersCurrentYear(limit);
    }

    @GetMapping("/wins/last-month")
    public List<PlayerWinsRankingDto> winsLastMonth(@RequestParam(defaultValue = "10") int limit) {
        return rankingService.getTopWinnersLastMonth(limit);
    }
}
