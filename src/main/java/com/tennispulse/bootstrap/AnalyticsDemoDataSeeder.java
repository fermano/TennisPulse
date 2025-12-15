package com.tennispulse.bootstrap;

import com.tennispulse.domain.MatchEntity;
import com.tennispulse.domain.MatchStatus;
import com.tennispulse.domain.PlayerEntity;
import com.tennispulse.domain.analytics.*;
import com.tennispulse.repository.MatchRepository;
import com.tennispulse.repository.analytics.PlayerMatchAnalyticsRepository;
import com.tennispulse.service.analytics.CoachingRuleEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@Component
@Profile("seed")
@Order(2) // runs AFTER DemoDataSeeder
@RequiredArgsConstructor
public class AnalyticsDemoDataSeeder implements CommandLineRunner {

    private final MatchRepository matchRepository;
    private final PlayerMatchAnalyticsRepository analyticsRepository;
    private final CoachingRuleEngine coachingRuleEngine;

    private static final int ANALYTICS_SAMPLES_PER_MATCH = 1;
    private static final int MONTHS_BACK = 12;

    private final Random random = new Random();

    @Override
    public void run(String... args) {
        if (analyticsRepository.count() > 0) {
            log.info("Analytics documents already exist, skipping AnalyticsDemoDataSeeder.");
            return;
        }

        List<MatchEntity> matches = matchRepository.findAll();
        if (matches.isEmpty()) {
            log.warn("No matches found in DB. Make sure DemoDataSeeder / Flyway demo data has run first.");
            return;
        }

        long docsCreated = 0L;

        for (MatchEntity match : matches) {
            if (match.getPlayer1() == null || match.getPlayer2() == null) {
                continue;
            }
            if (match.getStatus() != MatchStatus.COMPLETED) {
                continue; // only completed matches
            }

            for (int i = 0; i < ANALYTICS_SAMPLES_PER_MATCH; i++) {
                docsCreated += createAnalyticsForPlayer(match, match.getPlayer1());
                docsCreated += createAnalyticsForPlayer(match, match.getPlayer2());
            }
        }

        log.info("AnalyticsDemoDataSeeder completed. Created {} PlayerMatchAnalytics documents.", docsCreated);
    }

    private long createAnalyticsForPlayer(MatchEntity match, PlayerEntity player) {
        // 1) Synthetic raw stats
        PlayerStatsPayload rawStats = randomStatsForPlayer(player);

        // 2) Map to rawMetrics for rule engine
        Map<AnalyticsMetric, Double> rawMetrics = mapToRawMetrics(rawStats);

        // 3) Run rule engine (createdAt inside analysis can stay as "now")
        PlayerMatchCoachingAnalysis analysis =
                coachingRuleEngine.analyze(match.getId(), player.getId(), rawMetrics);

        // 4) Build the Mongo document manually, overriding createdAt
        PlayerMatchAnalyticsDocument doc = new PlayerMatchAnalyticsDocument();

        // id: matchId:playerId (same convention as your static from(...) methods)
        doc.setId(match.getId() + ":" + player.getId());

        // keep the same types you currently have in the document (UUIDs)
        doc.setMatchId(match.getId());
        doc.setPlayerId(player.getId());
        doc.setWinnerId(match.getWinner() != null ? match.getWinner().getId() : null);
        doc.setFinalScore(match.getFinalScore());

        // raw stats from the synthetic payload
        doc.setRawStats(rawStats);

        // coaching info from analysis
        doc.setCoachingStatus(analysis.getCoachingStatus());
        doc.setMetrics(analysis.getMetrics());
        doc.setTips(analysis.getTips());
        doc.setEngineVersion(analysis.getEngineVersion());

        // 5) HERE: use a random Instant in the last 12 months
        doc.setCreatedAt(randomInstantWithinLastMonths(MONTHS_BACK));

        analyticsRepository.save(doc);
        return 1L;
    }


    // ─────────── helpers ───────────

    private PlayerStatsPayload randomStatsForPlayer(PlayerEntity player) {
        PlayerStatsPayload p = new PlayerStatsPayload();
        p.setPlayerId(player.getId());
        p.setFirstServeIn(randomRange(50, 85));
        p.setFirstServePointsWon(randomRange(55, 85));
        p.setSecondServePointsWon(randomRange(40, 75));
        p.setUnforcedErrorsForehand(randomIntRange(2, 22));
        p.setUnforcedErrorsBackhand(randomIntRange(3, 24));
        p.setWinners(randomIntRange(8, 35));
        p.setBreakPointConversion(randomRange(20, 70));
        p.setBreakPointsSaved(randomRange(20, 80));
        p.setNetPointsWon(randomRange(35, 80));
        p.setLongRallyWinRate(randomRange(25, 75));
        return p;
    }

    private Map<AnalyticsMetric, Double> mapToRawMetrics(PlayerStatsPayload s) {
        return Map.of(
                AnalyticsMetric.FIRST_SERVE_IN, s.getFirstServeIn(),
                AnalyticsMetric.FIRST_SERVE_POINTS_WON, s.getFirstServePointsWon(),
                AnalyticsMetric.SECOND_SERVE_POINTS_WON, s.getSecondServePointsWon(),
                AnalyticsMetric.UNFORCED_ERRORS_FOREHAND, s.getUnforcedErrorsForehand().doubleValue(),
                AnalyticsMetric.UNFORCED_ERRORS_BACKHAND, s.getUnforcedErrorsBackhand().doubleValue(),
                AnalyticsMetric.WINNERS, s.getWinners().doubleValue(),
                AnalyticsMetric.BREAK_POINT_CONVERSION, s.getBreakPointConversion(),
                AnalyticsMetric.BREAK_POINTS_SAVED, s.getBreakPointsSaved(),
                AnalyticsMetric.NET_POINTS_WON, s.getNetPointsWon(),
                AnalyticsMetric.LONG_RALLY_WIN_RATE, s.getLongRallyWinRate()
        );
    }

    private double randomRange(int min, int max) {
        return min + (random.nextDouble() * (max - min));
    }

    private int randomIntRange(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private Instant randomInstantWithinLastMonths(int monthsBack) {
        LocalDate today = LocalDate.now();
        int maxDays = monthsBack * 30; // coarse but fine for demo

        int daysAgo = random.nextInt(maxDays + 1); // 0..maxDays
        LocalDate randomDate = today.minusDays(daysAgo);

        int hour = random.nextInt(24);
        int minute = random.nextInt(60);

        LocalDateTime randomDateTime = randomDate.atTime(hour, minute);

        return randomDateTime.toInstant(ZoneOffset.UTC);
    }
}
