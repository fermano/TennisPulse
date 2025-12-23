package com.tennispulse.service.analytics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennispulse.api.controllers.MatchController;
import com.tennispulse.domain.MatchCompletedEvent;
import com.tennispulse.domain.MatchEntity;
import com.tennispulse.domain.PlayerEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SqsMatchEventPublisherTest {

    @Mock
    SqsClient sqsClient;

    @Mock
    ObjectMapper objectMapper;

    SqsMatchEventPublisher publisher;

    final String queueUrl = "http://localhost:4566/000000000000/match-completed-queue";

    @BeforeEach
    void setUp() {
        publisher = new SqsMatchEventPublisher(sqsClient, objectMapper, queueUrl);
    }

    @Test
    void publishMatchCompleted_happyPath_shouldSerializeAndSendToSqs() throws Exception {
        // given
        String matchId = UUID.randomUUID().toString();
        String winnerId = UUID.randomUUID().toString();

        MatchEntity match = mock(MatchEntity.class);
        PlayerEntity winner = mock(PlayerEntity.class);

        when(match.getId()).thenReturn(matchId);
        when(match.getWinner()).thenReturn(winner);
        when(winner.getId()).thenReturn(winnerId);
        when(match.getFinalScore()).thenReturn("6-4 6-3");

        MatchController.PlayerStatsRequest statsRequest = new MatchController.PlayerStatsRequest();
        statsRequest.setPlayerId(UUID.randomUUID().toString());
        // you can fill other fields if you want, but it's not required for this test

        List<MatchController.PlayerStatsRequest> stats = List.of(statsRequest);

        // we don't care about the actual JSON content here, only that it's used
        when(objectMapper.writeValueAsString(any(MatchCompletedEvent.class)))
                .thenReturn("{\"dummy\":\"json\"}");

        // when
        publisher.publishMatchCompleted(match, stats);

        // then
        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient, times(1)).sendMessage(captor.capture());

        SendMessageRequest sent = captor.getValue();
        assertThat(sent.queueUrl()).isEqualTo(queueUrl);
        assertThat(sent.messageBody()).isEqualTo("{\"dummy\":\"json\"}");

        // and ObjectMapper was used to serialize the event
        verify(objectMapper, times(1)).writeValueAsString(any(MatchCompletedEvent.class));
    }

    @Test
    void publishMatchCompleted_whenSerializationFails_shouldNotSendMessage() throws Exception {
        // given
        String matchId = UUID.randomUUID().toString();
        String winnerId = UUID.randomUUID().toString();

        MatchEntity match = mock(MatchEntity.class);
        PlayerEntity winner = mock(PlayerEntity.class);

        when(match.getId()).thenReturn(matchId);
        when(match.getWinner()).thenReturn(winner);
        when(winner.getId()).thenReturn(winnerId);
        when(match.getFinalScore()).thenReturn("6-4 6-3");

        List<MatchController.PlayerStatsRequest> stats = Collections.emptyList();

        when(objectMapper.writeValueAsString(any(MatchCompletedEvent.class)))
                .thenThrow(new JsonProcessingException("boom") {});

        // when
        publisher.publishMatchCompleted(match, stats);

        // then
        verify(sqsClient, never()).sendMessage(any(SendMessageRequest.class));
        // and we did try to serialize
        verify(objectMapper, times(1)).writeValueAsString(any(MatchCompletedEvent.class));
    }
}

