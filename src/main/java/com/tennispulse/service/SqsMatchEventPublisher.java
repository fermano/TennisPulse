package com.tennispulse.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennispulse.api.MatchController;
import com.tennispulse.domain.MatchCompletedEvent;
import com.tennispulse.domain.MatchEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqsMatchEventPublisher {
    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final String matchCompletedQueueUrl;

    public void publishMatchCompleted(MatchEntity match,
                                      List<MatchController.PlayerStatsRequest> playerStats) {

        MatchCompletedEvent event = new MatchCompletedEvent();
        event.setMatchId(match.getId());
        event.setWinnerId(match.getWinner().getId());
        event.setFinalScore(match.getFinalScore());
        event.setOccurredAt(Instant.now());

        if (!CollectionUtils.isEmpty(playerStats)) {
            event.setPlayerStats(
                    playerStats.stream()
                            .map(this::toPayload)
                            .collect(Collectors.toList())
            );
        }

        try {
            String body = objectMapper.writeValueAsString(event);

            SendMessageRequest req = SendMessageRequest.builder()
                    .queueUrl(matchCompletedQueueUrl)
                    .messageBody(body)
                    .build();

            sqsClient.sendMessage(req);
            log.info("Published MatchCompleted event for match {}", match.getId());

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize MatchCompletedEvent for match {}", match.getId(), e);
        }
    }

    private MatchCompletedEvent.PlayerStatsPayload toPayload(MatchController.PlayerStatsRequest playerStatsRequest) {
            return objectMapper.convertValue(playerStatsRequest, MatchCompletedEvent.PlayerStatsPayload.class);
    }
}
