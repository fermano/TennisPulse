package com.tennispulse.service.analytics;

import com.tennispulse.api.analytics.dto.TimelineRange;
import com.tennispulse.api.analytics.dto.HighlightCategory;
import com.tennispulse.api.analytics.dto.HighlightsDashboardResponse;
import com.tennispulse.api.analytics.dto.PlayerHighlightDto;
import com.tennispulse.domain.PlayerEntity;
import com.tennispulse.domain.analytics.AnalyticsMetric;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerHighlightsService {

    private final MongoTemplate mongoTemplate;
    private final com.tennispulse.repository.PlayerRepository playerRepository;

    public HighlightsDashboardResponse getHighlights(TimelineRange range) {
        LocalDateTime from = computeStartDate(range);

        // 1) Aggregate per player
        List<PlayerAggregate> aggregates = aggregatePerPlayer(from);

        // 2) Compute highlights
        Map<HighlightCategory, PlayerHighlightDto> highlights = new EnumMap<>(HighlightCategory.class);

        bestServeHighlight(aggregates).ifPresent(h -> highlights.put(HighlightCategory.BEST_SERVE, h));
        bestRallyHighlight(aggregates).ifPresent(h -> highlights.put(HighlightCategory.BEST_RALLY_PLAYER, h));
        bestNetHighlight(aggregates).ifPresent(h -> highlights.put(HighlightCategory.BEST_NET_PLAYER, h));
        bestPressureHighlight(aggregates).ifPresent(h -> highlights.put(HighlightCategory.BEST_PRESSURE_PLAYER, h));
        cleanestBaselineHighlight(aggregates).ifPresent(h -> highlights.put(HighlightCategory.CLEANEST_BASELINE, h));

        return new HighlightsDashboardResponse(range, highlights);
    }

    private List<PlayerAggregate> aggregatePerPlayer(LocalDateTime from) {
        List<AggregationOperation> pipeline = new ArrayList<>();

        Criteria criteria = new Criteria();
        if (from != null) {
            criteria = Criteria.where("createdAt").gte(from);
        }

        pipeline.add(Aggregation.match(criteria));

        GroupOperation group = Aggregation.group("playerId")
                .first("playerId").as("playerId");

        // build averages for each metric
        for (AnalyticsMetric metric : AnalyticsMetric.values()) {
            group = group.avg("metrics." + metric.name() + ".value").as(metric.name());
        }

        pipeline.add(group);

        // sort by playerId just for determinism
        pipeline.add(Aggregation.sort(Sort.by(Sort.Direction.ASC, "playerId")));

        Aggregation agg = Aggregation.newAggregation(pipeline);

        List<Document> docs = mongoTemplate.aggregate(agg, "player_match_analytics", Document.class)
                .getMappedResults();

        return docs.stream()
                .map(this::toAggregate)
                .collect(Collectors.toList());
    }

    private PlayerAggregate toAggregate(Document doc) {
        String playerId = doc.getString("playerId");

        Map<AnalyticsMetric, Double> metrics = new EnumMap<>(AnalyticsMetric.class);
        for (AnalyticsMetric m : AnalyticsMetric.values()) {
            Double v = doc.getDouble(m.name());
            if (v != null) {
                metrics.put(m, v);
            }
        }

        return new PlayerAggregate(playerId, metrics);
    }

    // internal holder
    private record PlayerAggregate(String playerId, Map<AnalyticsMetric, Double> metrics) {}

    private Optional<PlayerHighlightDto> bestServeHighlight(List<PlayerAggregate> players) {
        return players.stream()
                .map(p -> {
                    double firstIn = valueOrZero(p, AnalyticsMetric.FIRST_SERVE_IN);
                    double firstWon = valueOrZero(p, AnalyticsMetric.FIRST_SERVE_POINTS_WON);
                    double secondWon = valueOrZero(p, AnalyticsMetric.SECOND_SERVE_POINTS_WON);

                    double score = 0.4 * firstIn + 0.3 * firstWon + 0.3 * secondWon;

                    return toHighlight(p.playerId(), score, Map.of(
                            "FIRST_SERVE_IN", firstIn,
                            "FIRST_SERVE_POINTS_WON", firstWon,
                            "SECOND_SERVE_POINTS_WON", secondWon
                    ));
                })
                .max(Comparator.comparingDouble(PlayerHighlightDto::score));
    }

    private Optional<PlayerHighlightDto> bestRallyHighlight(List<PlayerAggregate> players) {
        return players.stream()
                .map(p -> {
                    double longRally = valueOrZero(p, AnalyticsMetric.LONG_RALLY_WIN_RATE);
                    double fhErrors = valueOrZero(p, AnalyticsMetric.UNFORCED_ERRORS_FOREHAND);
                    double bhErrors = valueOrZero(p, AnalyticsMetric.UNFORCED_ERRORS_BACKHAND);
                    double totalErrors = fhErrors + bhErrors;

                    double errorScore = Math.max(0.0, 100.0 - (totalErrors / 30.0) * 100.0);
                    double score = 0.6 * longRally + 0.4 * errorScore;

                    return toHighlight(p.playerId(), score, Map.of(
                            "LONG_RALLY_WIN_RATE", longRally,
                            "UNFORCED_ERRORS_FOREHAND", fhErrors,
                            "UNFORCED_ERRORS_BACKHAND", bhErrors,
                            "TOTAL_ERRORS", totalErrors
                    ));
                })
                .max(Comparator.comparingDouble(PlayerHighlightDto::score));
    }

    private Optional<PlayerHighlightDto> bestNetHighlight(List<PlayerAggregate> players) {
        return players.stream()
                .map(p -> {
                    double netWon = valueOrZero(p, AnalyticsMetric.NET_POINTS_WON);
                    double winners = valueOrZero(p, AnalyticsMetric.WINNERS);

                    double winnersScore = Math.min(100.0, (winners / 30.0) * 100.0);
                    double score = 0.7 * netWon + 0.3 * winnersScore;

                    return toHighlight(p.playerId(), score, Map.of(
                            "NET_POINTS_WON", netWon,
                            "WINNERS", winners,
                            "WINNERS_SCORE", winnersScore
                    ));
                })
                .max(Comparator.comparingDouble(PlayerHighlightDto::score));
    }

    private Optional<PlayerHighlightDto> bestPressureHighlight(List<PlayerAggregate> players) {
        return players.stream()
                .map(p -> {
                    double conv = valueOrZero(p, AnalyticsMetric.BREAK_POINT_CONVERSION);
                    double saved = valueOrZero(p, AnalyticsMetric.BREAK_POINTS_SAVED);

                    double score = 0.5 * conv + 0.5 * saved;

                    return toHighlight(p.playerId(), score, Map.of(
                            "BREAK_POINT_CONVERSION", conv,
                            "BREAK_POINTS_SAVED", saved
                    ));
                })
                .max(Comparator.comparingDouble(PlayerHighlightDto::score));
    }

    private Optional<PlayerHighlightDto> cleanestBaselineHighlight(List<PlayerAggregate> players) {
        return players.stream()
                .map(p -> {
                    double fhErrors = valueOrZero(p, AnalyticsMetric.UNFORCED_ERRORS_FOREHAND);
                    double bhErrors = valueOrZero(p, AnalyticsMetric.UNFORCED_ERRORS_BACKHAND);
                    double totalErrors = fhErrors + bhErrors;

                    // turn into score so we can use max()
                    double score = Math.max(0.0, 100.0 - (totalErrors / 30.0) * 100.0);

                    return toHighlight(p.playerId(), score, Map.of(
                            "UNFORCED_ERRORS_FOREHAND", fhErrors,
                            "UNFORCED_ERRORS_BACKHAND", bhErrors,
                            "TOTAL_ERRORS", totalErrors
                    ));
                })
                .max(Comparator.comparingDouble(PlayerHighlightDto::score));
    }

    private double valueOrZero(PlayerAggregate p, AnalyticsMetric metric) {
        return p.metrics().getOrDefault(metric, 0.0);
    }

    private PlayerHighlightDto toHighlight(String playerId,
                                           double score,
                                           Map<String, Double> details) {
        PlayerEntity player = playerRepository.findById(playerId)
                .orElse(null);

        String name = player != null ? player.getName() : "Unknown Player";

        return new PlayerHighlightDto(playerId, name, score, details);
    }

    private LocalDateTime computeStartDate(TimelineRange range) {
        LocalDate today = LocalDate.now();

        return switch (range) {
            case ALL_TIME -> null;
            case LAST_MONTH -> today.minusMonths(1).atStartOfDay();
            case LAST_6_MONTHS -> today.minusMonths(6).atStartOfDay();
            case LAST_12_MONTHS -> today.minusMonths(12).atStartOfDay();
            case YEAR_TO_DATE -> today.withDayOfYear(1).atStartOfDay();
        };
    }
}

