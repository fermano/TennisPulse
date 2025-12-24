package com.tennispulse.service;

import com.tennispulse.api.dto.PlayerWinsRankingDto;
import com.tennispulse.domain.analytics.*;
import com.tennispulse.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {

    private final MatchRepository matchRepository;

    /**
     * Most wins in the current year.
     */
    @Cacheable(value = "rankings", key = "'wins:current-year:limit:' + #limit")
    public List<PlayerWinsRankingDto> getTopWinnersCurrentYear(int limit) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime startOfYear = now.withDayOfYear(1).toLocalDate().atStartOfDay(now.getZone());
        return getTopWinnersBetween(startOfYear.toInstant(), now.toInstant(), limit);
    }

    /**
     * Most wins in the last 30 days.
     */
    @Cacheable(value = "rankings", key = "'wins:last-month:limit:' + #limit")
    public List<PlayerWinsRankingDto> getTopWinnersLastMonth(int limit) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime start = now.minusDays(30).toLocalDate().atStartOfDay(now.getZone());
        List<PlayerWinsRankingDto> result = getTopWinnersBetween(start.toInstant(), now.toInstant(), limit);
        log.info("Computed rankings type={}, firstElemType={}",
                result.getClass(),
                result.isEmpty() ? "n/a" : result.getFirst().getClass());
        return result;
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

    @CacheEvict(value = "rankings", allEntries = true)
    public void invalidateRankingsCache() {
        log.debug("Rankings cache invalidated");
    }
}
