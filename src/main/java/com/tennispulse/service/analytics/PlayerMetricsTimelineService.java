package com.tennispulse.service.analytics;

import com.tennispulse.api.analytics.dto.PlayerMetricsTimelineResponseDto;
import com.tennispulse.api.analytics.dto.PlayerMonthlyMetricsDto;
import com.tennispulse.api.analytics.dto.TimelineRange;
import com.tennispulse.domain.analytics.AnalyticsMetric;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerMetricsTimelineService {

    private final MongoTemplate mongoTemplate;

    public PlayerMetricsTimelineResponseDto getPlayerTimeline(UUID playerId, TimelineRange range) {

        LocalDateTime from = computeStartDate(range);

        List<AggregationOperation> pipeline = new ArrayList<>();

        // Match documents for this player
        Criteria criteria = Criteria.where("playerId").is(playerId.toString());
        if (from != null) {
            criteria = criteria.and("createdAt").gte(from);
        }

        pipeline.add(Aggregation.match(criteria));

        // Add synthetic year/month fields
        pipeline.add(Aggregation.project()
                .andExpression("year(createdAt)").as("year")
                .andExpression("month(createdAt)").as("month")
                .and("metrics").as("metrics"));

        // Group → average each metric per (year, month)
        GroupOperation groupByMonth = Aggregation.group("year", "month")
                .first("year").as("year")
                .first("month").as("month");

        // Dynamically add averages for all metrics (use .value)
        for (AnalyticsMetric metric : AnalyticsMetric.values()) {
            groupByMonth = groupByMonth
                    .avg("metrics." + metric.name() + ".value")
                    .as(metric.name());
        }
        pipeline.add(groupByMonth);

        // Sort descending by date (newest first)
        pipeline.add(Aggregation.sort(Sort.by(Sort.Direction.DESC, "year", "month")));

        Aggregation agg = Aggregation.newAggregation(pipeline);

        List<Document> results = mongoTemplate.aggregate(agg, "player_match_analytics", Document.class)
                .getMappedResults();

        List<PlayerMonthlyMetricsDto> timeline = results.stream()
                .map(this::toMonthlyDto)
                .collect(Collectors.toList());

        Map<AnalyticsMetric, Double> overallAverages = computeOverallAverages(timeline);

        return new PlayerMetricsTimelineResponseDto(
                playerId.toString(),
                range,
                timeline,
                overallAverages
        );
    }

    private PlayerMonthlyMetricsDto toMonthlyDto(Document doc) {
        Integer year = doc.getInteger("year");
        Integer month = doc.getInteger("month");

        if (year == null || month == null) {
            log.warn("Skipping monthly analytics document without year/month: {}", doc.toJson());
            return null;
        }

        YearMonth ym = YearMonth.of(year, month);

        Map<AnalyticsMetric, Double> values = new EnumMap<>(AnalyticsMetric.class);
        for (AnalyticsMetric m : AnalyticsMetric.values()) {
            Double val = doc.getDouble(m.name());
            if (val != null) {
                values.put(m, val);
            }
        }

        return new PlayerMonthlyMetricsDto(ym, values);
    }


    private Map<AnalyticsMetric, Double> computeOverallAverages(List<PlayerMonthlyMetricsDto> timeline) {
        Map<AnalyticsMetric, List<Double>> buckets = new EnumMap<>(AnalyticsMetric.class);

        for (PlayerMonthlyMetricsDto month : timeline) {
            month.averages().forEach((metric, value) -> {
                buckets.computeIfAbsent(metric, k -> new ArrayList<>()).add(value);
            });
        }

        return buckets.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0)
                ));
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
