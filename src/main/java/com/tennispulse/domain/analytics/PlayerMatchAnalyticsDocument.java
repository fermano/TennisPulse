package com.tennispulse.domain.analytics;

import com.tennispulse.domain.MatchCompletedEvent;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Document(collection = "player_match_analytics")
public class PlayerMatchAnalyticsDocument {

    @Id
    private String id; // matchId:playerId

    private UUID matchId;
    private UUID playerId;

    private UUID winnerId;
    private String finalScore;

    private PlayerStatsPayload rawStats;

    private CoachingStatus coachingStatus;
    private Map<AnalyticsMetric, MetricValue> metrics;
    private List<CoachingTip> tips;

    private String engineVersion;
    private Instant createdAt;

    public static PlayerMatchAnalyticsDocument from(
            MatchCompletedEvent event,
            PlayerStatsPayload stats,
            PlayerMatchCoachingAnalysis analysis
    ) {
        PlayerMatchAnalyticsDocument doc = new PlayerMatchAnalyticsDocument();
        doc.id = event.getMatchId() + ":" + stats.getPlayerId();
        doc.matchId = event.getMatchId();
        doc.playerId = stats.getPlayerId();
        doc.winnerId = event.getWinnerId();
        doc.finalScore = event.getFinalScore();
        doc.rawStats = stats;
        doc.coachingStatus = analysis.getCoachingStatus();
        doc.metrics = analysis.getMetrics();
        doc.tips = analysis.getTips();
        doc.engineVersion = analysis.getEngineVersion();
        doc.createdAt = analysis.getCreatedAt();

        return doc;
    }
}

