package com.tennispulse.service;

import com.tennispulse.api.dto.PlayerWinsRankingDto;
import com.tennispulse.domain.analytics.*;
import com.tennispulse.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final MatchRepository matchRepository;

    /**
     * Most wins in the current year.
     */
    public List<PlayerWinsRankingDto> getTopWinnersCurrentYear(int limit) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime startOfYear = now.withDayOfYear(1).toLocalDate().atStartOfDay(now.getZone());
        return getTopWinnersBetween(startOfYear.toInstant(), now.toInstant(), limit);
    }

    /**
     * Most wins in the last 30 days.
     */
    public List<PlayerWinsRankingDto> getTopWinnersLastMonth(int limit) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime start = now.minusDays(30).toLocalDate().atStartOfDay(now.getZone());
        return getTopWinnersBetween(start.toInstant(), now.toInstant(), limit);
    }

    private List<PlayerWinsRankingDto> getTopWinnersBetween(Instant from, Instant to, int limit) {
        List<Object[]> rows = matchRepository.findWinCountsBetween(from, to);

        return rows.stream()
                .map(r -> {
                    String playerId = (String) r[0];
                    String playerName = (String) r[1];
                    long wins = (long) r[2];
                    return new PlayerWinsRankingDto(playerId, playerName, wins);
                })
                .limit(limit)
                .toList();
    }
}
