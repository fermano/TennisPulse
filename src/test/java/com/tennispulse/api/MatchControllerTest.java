package com.tennispulse.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennispulse.api.controllers.MatchController;
import com.tennispulse.domain.Handedness;
import com.tennispulse.domain.MatchEntity;
import com.tennispulse.domain.MatchStatus;
import com.tennispulse.domain.PlayerEntity;
import com.tennispulse.service.MatchQueryService;
import com.tennispulse.service.MatchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(MatchController.class)
class MatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MatchService matchService;

    @MockBean
    private MatchQueryService matchQueryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void list_shouldReturnListOfMatches() throws Exception {
        String matchId = UUID.randomUUID().toString();
        String clubId = UUID.randomUUID().toString();
        String p1Id = UUID.randomUUID().toString();
        String p2Id = UUID.randomUUID().toString();

        MatchController.MatchResponse response = new MatchController.MatchResponse(
                matchId,
                clubId,
                "Green Valley",
                p1Id,
                "Alice",
                p2Id,
                "Bruno",
                null,
                null,
                null,
                MatchStatus.SCHEDULED,
                Instant.parse("2025-01-01T10:00:00Z"),
                null,
                Instant.parse("2025-01-01T10:00:00Z"),
                Instant.parse("2025-01-01T10:00:00Z")
        );

        when(matchQueryService.listAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(matchId.toString())))
                .andExpect(jsonPath("$[0].clubId", is(clubId.toString())))
                .andExpect(jsonPath("$[0].player1Id", is(p1Id.toString())))
                .andExpect(jsonPath("$[0].player2Id", is(p2Id.toString())))
                .andExpect(jsonPath("$[0].status", is("SCHEDULED")));
    }

    @Test
    void get_shouldReturnSingleMatch() throws Exception {
        String matchId = UUID.randomUUID().toString();
        String p1Id = UUID.randomUUID().toString();
        String p2Id = UUID.randomUUID().toString();

        MatchController.MatchResponse response = new MatchController.MatchResponse(
                matchId,
                null,
                null,
                p1Id,
                "Alice",
                p2Id,
                "Bruno",
                null,
                null,
                null,
                MatchStatus.SCHEDULED,
                null,
                null,
                Instant.parse("2025-01-01T10:00:00Z"),
                Instant.parse("2025-01-01T10:00:00Z")
        );

        when(matchQueryService.getById(matchId)).thenReturn(response);

        mockMvc.perform(get("/api/matches/{id}", matchId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(matchId.toString())))
                .andExpect(jsonPath("$.player1Id", is(p1Id.toString())))
                .andExpect(jsonPath("$.player2Id", is(p2Id.toString())))
                .andExpect(jsonPath("$.status", is("SCHEDULED")));
    }

    @Test
    void updateStatus_shouldCallServiceAndReturnUpdatedMatch() throws Exception {
        String matchId = UUID.randomUUID().toString();
        String winnerId = UUID.randomUUID().toString();

        MatchController.UpdateMatchStatusRequest request = new MatchController.UpdateMatchStatusRequest();
        request.setStatus(MatchStatus.COMPLETED);
        request.setWinnerId(winnerId);
        request.setFinalScore("6-4 6-3");

        MatchController.MatchResponse response = new MatchController.MatchResponse(
                matchId,
                null,
                null,
                UUID.randomUUID().toString(),
                "Alice",
                UUID.randomUUID().toString(),
                "Bruno",
                winnerId,
                "Alice",
                "6-4 6-3",
                MatchStatus.COMPLETED,
                Instant.parse("2025-01-01T10:00:00Z"),
                Instant.parse("2025-01-01T11:10:00Z"),
                Instant.parse("2025-01-01T10:00:00Z"),
                Instant.parse("2025-01-01T10:00:00Z")
        );

        // stub updateStatus to return some MatchEntity (controller might not use it, but Mockito needs a return)
        MatchEntity updatedEntity = new MatchEntity();
        updatedEntity.setId(matchId);
        updatedEntity.setStatus(MatchStatus.COMPLETED);
        PlayerEntity winner = PlayerEntity.builder()
                .id(UUID.randomUUID().toString())
                .name("Alice")
                .handedness(Handedness.RIGHT)
                .build();
        updatedEntity.setWinner(winner);
        updatedEntity.setFinalScore("6-4 6-3");

        when(matchService.updateStatus(matchId, request)).thenReturn(updatedEntity);

        when(matchQueryService.getById(matchId)).thenReturn(response);

        mockMvc.perform(put("/api/matches/{id}/status", matchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(matchId.toString())))
                .andExpect(jsonPath("$.winnerId", is(winnerId.toString())))
                .andExpect(jsonPath("$.finalScore", is("6-4 6-3")))
                .andExpect(jsonPath("$.status", is("COMPLETED")));

        // optional but matches the test name: verify service was called correctly
        verify(matchService).updateStatus(matchId, request);
    }

}
