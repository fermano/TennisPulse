package com.tennispulse.bootstrap;

import com.tennispulse.domain.ClubEntity;
import com.tennispulse.domain.MatchEntity;
import com.tennispulse.domain.MatchStatus;
import com.tennispulse.domain.PlayerEntity;
import com.tennispulse.domain.analytics.AnalyticsMetric;
import com.tennispulse.domain.analytics.PlayerMatchAnalyticsDocument;
import com.tennispulse.domain.analytics.PlayerMatchCoachingAnalysis;
import com.tennispulse.domain.analytics.PlayerStatsPayload;
import com.tennispulse.repository.ClubRepository;
import com.tennispulse.repository.MatchRepository;
import com.tennispulse.repository.PlayerRepository;
import com.tennispulse.repository.analytics.PlayerMatchAnalyticsRepository;
import com.tennispulse.service.analytics.CoachingRuleEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Component
@Profile("seed")
@Order(1)
@RequiredArgsConstructor
public class DemoDataSeeder implements CommandLineRunner {

    private final PlayerRepository playerRepository;
    private final ClubRepository clubRepository;
    private final MatchRepository matchRepository;
    private final PlayerMatchAnalyticsRepository analyticsRepository;
    private final CoachingRuleEngine coachingRuleEngine;

    private static final int NUM_PLAYERS = 16;
    private static final int MATCHES_PER_PLAYER = 40;

    private final Random random = new Random();

    @Override
    @Transactional
    public void run(String... args) {
        if (playerRepository.count() > 0 && matchRepository.count() > 0) {
            log.info("Seed data already present, skipping DemoDataSeeder.");
            return;
        }

        log.info("Starting demo data seeding for Tennis Pulse...");

        List<ClubEntity> clubs = createClubs();
        List<PlayerEntity> players = createPlayers(clubs.get(0));

        seedMatchesAndAnalytics(players, clubs);

        log.info("Demo data seeding completed: players={}, matches={}, analyticsDocs={}",
                playerRepository.count(), matchRepository.count(), analyticsRepository.count());
    }

    private List<ClubEntity> createClubs() {
        List<ClubEntity> result = new ArrayList<>();

        result.add(clubRepository.save(ClubEntity.builder()
                .name("Center Court Club")
                .city("London")
                .country("UK")
                .build()));

        result.add(clubRepository.save(ClubEntity.builder()
                .name("Clay Masters Academy")
                .city("Madrid")
                .country("Spain")
                .build()));

        result.add(clubRepository.save(ClubEntity.builder()
                .name("Baseline Tennis Center")
                .city("New York")
                .country("USA")
                .build()));

        return result;
    }

    private List<PlayerEntity> createPlayers(ClubEntity defaultClub) {
        List<PlayerEntity> players = new ArrayList<>();

        List<String> names = List.of(
                "Alice", "Bruno", "Carla", "Diego",
                "Emma", "Felix", "Giulia", "Hiro",
                "Ines", "Jonas", "Klara", "Leo",
                "Marta", "Nico", "Olivia", "Pedro"
        );

        for (int i = 0; i < NUM_PLAYERS && i < names.size(); i++) {
            PlayerEntity p = PlayerEntity.builder()
                    .name(names.get(i))
                    .build();
            players.add(playerRepository.save(p));
        }

        return players;
    }

    private void seedMatchesAndAnalytics(List<PlayerEntity> players, List<ClubEntity> clubs) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now();

        for (PlayerEntity p1 : players) {
            for (int i = 0; i < MATCHES_PER_PLAYER; i++) {
                PlayerEntity p2 = pickRandomOpponent(players, p1);
                ClubEntity club = clubs.get(random.nextInt(clubs.size()));

                // Random date in last 24 months
                int daysAgo = random.nextInt(730); // 0–729 days
                LocalDate matchDate = today.minusDays(daysAgo);
                Instant startTime = matchDate.atTime(10 + random.nextInt(8), 0)
                        .atZone(zone).toInstant();
                Instant endTime = startTime.plusSeconds(60L * (60 + random.nextInt(60))); // 60–120 mins

                // Generate stats
                PlayerStats statsP1 = randomStatsForPlayer();
                PlayerStats statsP2 = randomStatsForPlayer();

                // Decide winner based on "score" from stats
                double score1 = computeScore(statsP1);
                double score2 = computeScore(statsP2);

                PlayerEntity winner = score1 >= score2 ? p1 : p2;
                String finalScore = randomFinalScoreForWinner(winner == p1);

                MatchEntity match = MatchEntity.builder()
                        .club(club)
                        .player1(p1)
                        .player2(p2)
                        .winner(winner)
                        .finalScore(finalScore)
                        .status(MatchStatus.COMPLETED)
                        .startTime(startTime)
                        .endTime(endTime)
                        .build();

                match = matchRepository.save(match);

            }
        }
    }

    private PlayerEntity pickRandomOpponent(List<PlayerEntity> players, PlayerEntity p1) {
        PlayerEntity p2;
        do {
            p2 = players.get(random.nextInt(players.size()));
        } while (p2.getId().equals(p1.getId()));
        return p2;
    }

    private PlayerStats randomStatsForPlayer() {
        PlayerStats s = new PlayerStats();
        s.firstServeIn = 50 + random.nextDouble() * 40;     // 50–90%
        s.firstServePointsWon = 55 + random.nextDouble() * 30; // 55–85%
        s.secondServePointsWon = 40 + random.nextDouble() * 30; // 40–70%

        s.unforcedErrorsForehand = 2 + random.nextInt(20);  // 2–21
        s.unforcedErrorsBackhand = 3 + random.nextInt(20);  // 3–22
        s.winners = 10 + random.nextInt(25);                // 10–34

        s.breakPointConversion = 20 + random.nextDouble() * 50; // 20–70%
        s.breakPointsSaved = 20 + random.nextDouble() * 60;     // 20–80%
        s.netPointsWon = 40 + random.nextDouble() * 40;         // 40–80%
        s.longRallyWinRate = 30 + random.nextDouble() * 40;     // 30–70%

        return s;
    }

    private double computeScore(PlayerStats s) {
        // Simple heuristic score used only for seeding the "winner".
        double goodPercent = (s.firstServeIn + s.firstServePointsWon + s.secondServePointsWon
                + s.breakPointConversion + s.breakPointsSaved + s.netPointsWon + s.longRallyWinRate) / 7.0;
        double errors = s.unforcedErrorsForehand + s.unforcedErrorsBackhand;
        return goodPercent - 0.5 * errors + 0.3 * s.winners;
    }

    private String randomFinalScoreForWinner(boolean p1Wins) {
        // Just a bit of flavor; not used analytically.
        List<String> scores = List.of("6-4 6-3", "7-6 6-4", "6-3 3-6 6-3", "6-2 6-2");
        return scores.get(random.nextInt(scores.size()));
    }

    /**
     * Internal helper struct for generating stats, mapped later to your raw stats payload.
     */
    private static class PlayerStats {
        double firstServeIn;
        double firstServePointsWon;
        double secondServePointsWon;
        int unforcedErrorsForehand;
        int unforcedErrorsBackhand;
        int winners;
        double breakPointConversion;
        double breakPointsSaved;
        double netPointsWon;
        double longRallyWinRate;

        public PlayerStatsPayload toRawStatsPayload() {
            PlayerStatsPayload p = new PlayerStatsPayload();
            p.setFirstServeIn(firstServeIn);
            p.setFirstServePointsWon(firstServePointsWon);
            p.setSecondServePointsWon(secondServePointsWon);
            p.setUnforcedErrorsForehand(unforcedErrorsForehand);
            p.setUnforcedErrorsBackhand(unforcedErrorsBackhand);
            p.setWinners(winners);
            p.setBreakPointConversion(breakPointConversion);
            p.setBreakPointsSaved(breakPointsSaved);
            p.setNetPointsWon(netPointsWon);
            p.setLongRallyWinRate(longRallyWinRate);
            return p;
        }
    }
}
