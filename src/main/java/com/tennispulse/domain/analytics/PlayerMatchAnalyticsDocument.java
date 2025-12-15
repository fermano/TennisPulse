package com.tennispulse.domain.analytics;

import com.tennispulse.domain.MatchCompletedEvent;
import com.tennispulse.domain.MatchEntity;
import com.tennispulse.domain.PlayerEntity;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "player_match_analytics")
public class PlayerMatchAnalyticsDocument {

    @Id
    private String id; // matchId:playerId

    // store as plain String in Mongo
    private String matchId;
    private String playerId;

    private String winnerId;
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
        // composite id as string
        doc.id = event.getMatchId() + ":" + stats.getPlayerId();

        // store String representation
        doc.matchId = event.getMatchId();
        doc.playerId = stats.getPlayerId();
        doc.winnerId = event.getWinnerId() != null ? event.getWinnerId() : null;

        doc.finalScore = event.getFinalScore();
        doc.rawStats = stats;
        doc.coachingStatus = analysis.getCoachingStatus();
        doc.metrics = analysis.getMetrics();
        doc.tips = analysis.getTips();
        doc.engineVersion = analysis.getEngineVersion();
        doc.createdAt = analysis.getCreatedAt();

        return doc;
    }

    public static PlayerMatchAnalyticsDocument from(
            MatchEntity match,
            PlayerEntity player,
            PlayerStatsPayload rawStats,
            PlayerMatchCoachingAnalysis analysis
    ) {
        PlayerMatchAnalyticsDocument doc = new PlayerMatchAnalyticsDocument();

        doc.id = match.getId() + ":" + player.getId();

        doc.matchId = match.getId();
        doc.playerId = player.getId();
        doc.winnerId = match.getWinner() != null ? match.getWinner().getId() : null;

        doc.finalScore = match.getFinalScore();
        doc.rawStats = rawStats;
        doc.coachingStatus = analysis.getCoachingStatus();
        doc.metrics = analysis.getMetrics();
        doc.tips = analysis.getTips();
        doc.engineVersion = analysis.getEngineVersion();
        doc.createdAt = analysis.getCreatedAt();

        return doc;
    }
}
