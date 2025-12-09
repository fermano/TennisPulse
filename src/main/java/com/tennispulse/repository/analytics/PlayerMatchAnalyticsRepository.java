package com.tennispulse.repository.analytics;

import com.tennispulse.domain.analytics.PlayerMatchAnalyticsDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface PlayerMatchAnalyticsRepository
        extends MongoRepository<PlayerMatchAnalyticsDocument, String> {

    List<PlayerMatchAnalyticsDocument> findByPlayerIdOrderByCreatedAtDesc(UUID playerId);
}
