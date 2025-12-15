package com.tennispulse.service;

import com.tennispulse.api.dto.PlayerPerformanceRankingDto;
import com.tennispulse.api.dto.PlayerWinsRankingDto;
import com.tennispulse.domain.PlayerEntity;
import com.tennispulse.domain.analytics.*;
import com.tennispulse.repository.MatchRepository;
import com.tennispulse.repository.PlayerRepository;
import com.tennispulse.repository.analytics.PlayerMatchAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final PlayerMatchAnalyticsRepository analyticsRepository;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;

    // Map statuses to numeric score
    private static final Map<MetricStatus, Integer> STATUS_SCORE = Map.of(
            MetricStatus.EXCELLENT, 3,
            MetricStatus.GOOD, 2,
            MetricStatus.WARNING, 1,
            MetricStatus.CRITICAL, 0
    );

    /**
     * Overall all-time performance ranking based on analytics metrics.
     */
    public List<PlayerPerformanceRankingDto> getTopOverallAllTime(int limit) {
        List<PlayerMatchAnalyticsDocument> docs = analyticsRepository.findAll();

        Map<String, List<PlayerMatchAnalyticsDocument>> byPlayer = docs.stream()
                .collect(Collectors.groupingBy(PlayerMatchAnalyticsDocument::getPlayerId));

        return byPlayer.entrySet().stream()
                .map(entry -> {
                    String playerId = entry.getKey();
                    List<PlayerMatchAnalyticsDocument> playerDocs = entry.getValue();

                    double avgScore = playerDocs.stream()
                            .mapToDouble(this::computeOverallScore)
                            .average()
                            .orElse(0.0);

                    long matchesCount = playerDocs.size();
                    String playerName = playerRepository.findById(playerId)
                            .map(PlayerEntity::getName)
                            .orElse("Unknown");

                    return new PlayerPerformanceRankingDto(playerId, playerName, avgScore, matchesCount);
                })
                .sorted(Comparator.comparingDouble(PlayerPerformanceRankingDto::getAverageScore).reversed())
                .limit(limit)
                .toList();
    }

    /**
     * Overall performance for current year only.
     */
    public List<PlayerPerformanceRankingDto> getTopOverallCurrentYear(int limit) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime startOfYear = now.withDayOfYear(1).toLocalDate().atStartOfDay(now.getZone());
        return getTopOverallBetween(startOfYear.toInstant(), now.toInstant(), limit);
    }

    /**
     * Overall performance for current month only.
     */
    public List<PlayerPerformanceRankingDto> getTopOverallCurrentMonth(int limit) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay(now.getZone());
        return getTopOverallBetween(startOfMonth.toInstant(), now.toInstant(), limit);
    }

    private List<PlayerPerformanceRankingDto> getTopOverallBetween(Instant from, Instant to, int limit) {
        List<PlayerMatchAnalyticsDocument> docs = analyticsRepository.findAll().stream()
                .filter(doc -> !doc.getCreatedAt().isBefore(from) && !doc.getCreatedAt().isAfter(to))
                .toList();

        Map<String, List<PlayerMatchAnalyticsDocument>> byPlayer = docs.stream()
                .collect(Collectors.groupingBy(PlayerMatchAnalyticsDocument::getPlayerId));

        return byPlayer.entrySet().stream()
                .map(entry -> {
                    String playerId = entry.getKey();
                    List<PlayerMatchAnalyticsDocument> playerDocs = entry.getValue();

                    double avgScore = playerDocs.stream()
                            .mapToDouble(this::computeOverallScore)
                            .average()
                            .orElse(0.0);

                    long matchesCount = playerDocs.size();
                    String playerName = playerRepository.findById(playerId)
                            .map(PlayerEntity::getName)
                            .orElse("Unknown");

                    return new PlayerPerformanceRankingDto(playerId, playerName, avgScore, matchesCount);
                })
                .sorted(Comparator.comparingDouble(PlayerPerformanceRankingDto::getAverageScore).reversed())
                .limit(limit)
                .toList();
    }

    private double computeOverallScore(PlayerMatchAnalyticsDocument doc) {
        if (doc.getMetrics() == null || doc.getMetrics().isEmpty()) {
            return 0.0;
        }

        return doc.getMetrics().values().stream()
                .mapToInt(mv -> STATUS_SCORE.getOrDefault(mv.getStatus(), 0))
                .average()
                .orElse(0.0);
    }

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
