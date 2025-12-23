package com.tennispulse.api.analytics.controllers;

import com.tennispulse.api.analytics.dto.HighlightCategory;
import com.tennispulse.api.analytics.dto.HighlightsDashboardResponse;
import com.tennispulse.api.analytics.dto.PlayerHighlightDto;
import com.tennispulse.api.analytics.dto.TimelineRange;
import com.tennispulse.service.analytics.PlayerHighlightsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(HighlightsController.class)
class HighlightsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlayerHighlightsService playerHighlightsService;

    @Test
    void getHighlights_shouldReturnHighlights_forLastMonth() throws Exception {
        // Arrange
        String player1Id = UUID.randomUUID().toString();
        String player2Id = UUID.randomUUID().toString();

        Map<HighlightCategory, PlayerHighlightDto> highlights = new EnumMap<>(HighlightCategory.class);
        highlights.put(HighlightCategory.BEST_SERVE,
                new PlayerHighlightDto(player1Id, "Rafael Nadal", 85.5, Map.of("FIRST_SERVE_IN", 90.0)));
        highlights.put(HighlightCategory.BEST_NET_PLAYER,
                new PlayerHighlightDto(player2Id, "Roger Federer", 88.0, Map.of("NET_POINTS_WON", 92.0)));

        HighlightsDashboardResponse response = new HighlightsDashboardResponse(
                TimelineRange.LAST_MONTH,
                highlights
        );

        when(playerHighlightsService.getHighlights(TimelineRange.LAST_MONTH)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/analytics/highlights/{range}", "LAST_MONTH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.range", is("LAST_MONTH")))
                .andExpect(jsonPath("$.highlights.BEST_SERVE.playerId", is(player1Id)))
                .andExpect(jsonPath("$.highlights.BEST_SERVE.playerName", is("Rafael Nadal")))
                .andExpect(jsonPath("$.highlights.BEST_SERVE.score", is(85.5)))
                .andExpect(jsonPath("$.highlights.BEST_NET_PLAYER.playerId", is(player2Id)))
                .andExpect(jsonPath("$.highlights.BEST_NET_PLAYER.playerName", is("Roger Federer")))
                .andExpect(jsonPath("$.highlights.BEST_NET_PLAYER.score", is(88.0)));

        verify(playerHighlightsService).getHighlights(TimelineRange.LAST_MONTH);
    }

    @Test
    void getHighlights_shouldReturnHighlights_forLast6Months() throws Exception {
        // Arrange
        String playerId = UUID.randomUUID().toString();

        Map<HighlightCategory, PlayerHighlightDto> highlights = new EnumMap<>(HighlightCategory.class);
        highlights.put(HighlightCategory.BEST_SERVE,
                new PlayerHighlightDto(playerId, "Novak Djokovic", 90.0, Map.of("FIRST_SERVE_IN", 95.0)));

        HighlightsDashboardResponse response = new HighlightsDashboardResponse(
                TimelineRange.LAST_6_MONTHS,
                highlights
        );

        when(playerHighlightsService.getHighlights(TimelineRange.LAST_6_MONTHS)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/analytics/highlights/{range}", "LAST_6_MONTHS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.range", is("LAST_6_MONTHS")))
                .andExpect(jsonPath("$.highlights.BEST_SERVE.playerId", is(playerId)))
                .andExpect(jsonPath("$.highlights.BEST_SERVE.playerName", is("Novak Djokovic")))
                .andExpect(jsonPath("$.highlights.BEST_SERVE.score", is(90.0)));

        verify(playerHighlightsService).getHighlights(TimelineRange.LAST_6_MONTHS);
    }

    @Test
    void getHighlights_shouldReturnHighlights_forLast12Months() throws Exception {
        // Arrange
        String playerId = UUID.randomUUID().toString();

        Map<HighlightCategory, PlayerHighlightDto> highlights = new EnumMap<>(HighlightCategory.class);
        highlights.put(HighlightCategory.BEST_PRESSURE_PLAYER,
                new PlayerHighlightDto(playerId, "Andy Murray", 82.5, Map.of("BREAK_POINT_CONVERSION", 75.0)));

        HighlightsDashboardResponse response = new HighlightsDashboardResponse(
                TimelineRange.LAST_12_MONTHS,
                highlights
        );

        when(playerHighlightsService.getHighlights(TimelineRange.LAST_12_MONTHS)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/analytics/highlights/{range}", "LAST_12_MONTHS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.range", is("LAST_12_MONTHS")))
                .andExpect(jsonPath("$.highlights.BEST_PRESSURE_PLAYER.playerId", is(playerId)))
                .andExpect(jsonPath("$.highlights.BEST_PRESSURE_PLAYER.playerName", is("Andy Murray")))
                .andExpect(jsonPath("$.highlights.BEST_PRESSURE_PLAYER.score", is(82.5)));

        verify(playerHighlightsService).getHighlights(TimelineRange.LAST_12_MONTHS);
    }

    @Test
    void getHighlights_shouldReturnHighlights_forYearToDate() throws Exception {
        // Arrange
        String playerId = UUID.randomUUID().toString();

        Map<HighlightCategory, PlayerHighlightDto> highlights = new EnumMap<>(HighlightCategory.class);
        highlights.put(HighlightCategory.CLEANEST_BASELINE,
                new PlayerHighlightDto(playerId, "Stan Wawrinka", 78.3,
                        Map.of("UNFORCED_ERRORS_FOREHAND", 5.0, "UNFORCED_ERRORS_BACKHAND", 3.0)));

        HighlightsDashboardResponse response = new HighlightsDashboardResponse(
                TimelineRange.YEAR_TO_DATE,
                highlights
        );

        when(playerHighlightsService.getHighlights(TimelineRange.YEAR_TO_DATE)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/analytics/highlights/{range}", "YEAR_TO_DATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.range", is("YEAR_TO_DATE")))
                .andExpect(jsonPath("$.highlights.CLEANEST_BASELINE.playerId", is(playerId)))
                .andExpect(jsonPath("$.highlights.CLEANEST_BASELINE.playerName", is("Stan Wawrinka")))
                .andExpect(jsonPath("$.highlights.CLEANEST_BASELINE.score", is(78.3)));

        verify(playerHighlightsService).getHighlights(TimelineRange.YEAR_TO_DATE);
    }

    @Test
    void getHighlights_shouldReturnHighlights_forAllTime() throws Exception {
        // Arrange
        String playerId = UUID.randomUUID().toString();

        Map<HighlightCategory, PlayerHighlightDto> highlights = new EnumMap<>(HighlightCategory.class);
        highlights.put(HighlightCategory.BEST_RALLY_PLAYER,
                new PlayerHighlightDto(playerId, "Rafael Nadal", 91.2,
                        Map.of("LONG_RALLY_WIN_RATE", 85.0)));

        HighlightsDashboardResponse response = new HighlightsDashboardResponse(
                TimelineRange.ALL_TIME,
                highlights
        );

        when(playerHighlightsService.getHighlights(TimelineRange.ALL_TIME)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/analytics/highlights/{range}", "ALL_TIME"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.range", is("ALL_TIME")))
                .andExpect(jsonPath("$.highlights.BEST_RALLY_PLAYER.playerId", is(playerId)))
                .andExpect(jsonPath("$.highlights.BEST_RALLY_PLAYER.playerName", is("Rafael Nadal")))
                .andExpect(jsonPath("$.highlights.BEST_RALLY_PLAYER.score", is(91.2)));

        verify(playerHighlightsService).getHighlights(TimelineRange.ALL_TIME);
    }

    @Test
    void getHighlights_shouldReturnEmptyHighlights_whenNoDataExists() throws Exception {
        // Arrange
        Map<HighlightCategory, PlayerHighlightDto> emptyHighlights = new EnumMap<>(HighlightCategory.class);

        HighlightsDashboardResponse response = new HighlightsDashboardResponse(
                TimelineRange.LAST_MONTH,
                emptyHighlights
        );

        when(playerHighlightsService.getHighlights(TimelineRange.LAST_MONTH)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/analytics/highlights/{range}", "LAST_MONTH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.range", is("LAST_MONTH")))
                .andExpect(jsonPath("$.highlights").isEmpty());

        verify(playerHighlightsService).getHighlights(TimelineRange.LAST_MONTH);
    }

    @Test
    void getHighlights_shouldReturnAllHighlightCategories_whenAllPresent() throws Exception {
        // Arrange
        String player1Id = UUID.randomUUID().toString();
        String player2Id = UUID.randomUUID().toString();
        String player3Id = UUID.randomUUID().toString();
        String player4Id = UUID.randomUUID().toString();
        String player5Id = UUID.randomUUID().toString();

        Map<HighlightCategory, PlayerHighlightDto> highlights = new EnumMap<>(HighlightCategory.class);
        highlights.put(HighlightCategory.BEST_SERVE,
                new PlayerHighlightDto(player1Id, "Player 1", 85.0, Map.of()));
        highlights.put(HighlightCategory.BEST_RALLY_PLAYER,
                new PlayerHighlightDto(player2Id, "Player 2", 82.0, Map.of()));
        highlights.put(HighlightCategory.BEST_NET_PLAYER,
                new PlayerHighlightDto(player3Id, "Player 3", 88.0, Map.of()));
        highlights.put(HighlightCategory.BEST_PRESSURE_PLAYER,
                new PlayerHighlightDto(player4Id, "Player 4", 79.0, Map.of()));
        highlights.put(HighlightCategory.CLEANEST_BASELINE,
                new PlayerHighlightDto(player5Id, "Player 5", 90.0, Map.of()));

        HighlightsDashboardResponse response = new HighlightsDashboardResponse(
                TimelineRange.LAST_MONTH,
                highlights
        );

        when(playerHighlightsService.getHighlights(TimelineRange.LAST_MONTH)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/analytics/highlights/{range}", "LAST_MONTH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.range", is("LAST_MONTH")))
                .andExpect(jsonPath("$.highlights.BEST_SERVE").exists())
                .andExpect(jsonPath("$.highlights.BEST_RALLY_PLAYER").exists())
                .andExpect(jsonPath("$.highlights.BEST_NET_PLAYER").exists())
                .andExpect(jsonPath("$.highlights.BEST_PRESSURE_PLAYER").exists())
                .andExpect(jsonPath("$.highlights.CLEANEST_BASELINE").exists());

        verify(playerHighlightsService).getHighlights(TimelineRange.LAST_MONTH);
    }

    @Test
    void getHighlights_shouldIncludeDetailsInResponse() throws Exception {
        // Arrange
        String playerId = UUID.randomUUID().toString();

        Map<String, Double> details = Map.of(
                "FIRST_SERVE_IN", 92.5,
                "FIRST_SERVE_POINTS_WON", 85.3,
                "SECOND_SERVE_POINTS_WON", 78.1
        );

        Map<HighlightCategory, PlayerHighlightDto> highlights = new EnumMap<>(HighlightCategory.class);
        highlights.put(HighlightCategory.BEST_SERVE,
                new PlayerHighlightDto(playerId, "Rafael Nadal", 87.2, details));

        HighlightsDashboardResponse response = new HighlightsDashboardResponse(
                TimelineRange.LAST_MONTH,
                highlights
        );

        when(playerHighlightsService.getHighlights(TimelineRange.LAST_MONTH)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/analytics/highlights/{range}", "LAST_MONTH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.highlights.BEST_SERVE.details.FIRST_SERVE_IN", is(92.5)))
                .andExpect(jsonPath("$.highlights.BEST_SERVE.details.FIRST_SERVE_POINTS_WON", is(85.3)))
                .andExpect(jsonPath("$.highlights.BEST_SERVE.details.SECOND_SERVE_POINTS_WON", is(78.1)));

        verify(playerHighlightsService).getHighlights(TimelineRange.LAST_MONTH);
    }

    @Test
    void getHighlights_shouldReturn400_forInvalidTimelineRange() throws Exception {
        // Act & Assert
        // Spring MVC handles invalid enum conversion and returns 400 Bad Request
        mockMvc.perform(get("/api/analytics/highlights/{range}", "INVALID_RANGE"))
                .andExpect(status().isBadRequest());

        // Service should not be called for invalid enum
        verify(playerHighlightsService, never()).getHighlights(any(TimelineRange.class));
    }

    @Test
    void getHighlights_shouldHandleSamePlayerInMultipleCategories() throws Exception {
        // Arrange
        String playerId = UUID.randomUUID().toString();

        Map<HighlightCategory, PlayerHighlightDto> highlights = new EnumMap<>(HighlightCategory.class);
        highlights.put(HighlightCategory.BEST_SERVE,
                new PlayerHighlightDto(playerId, "Dominant Player", 90.0, Map.of()));
        highlights.put(HighlightCategory.BEST_NET_PLAYER,
                new PlayerHighlightDto(playerId, "Dominant Player", 92.0, Map.of()));
        highlights.put(HighlightCategory.BEST_PRESSURE_PLAYER,
                new PlayerHighlightDto(playerId, "Dominant Player", 88.0, Map.of()));

        HighlightsDashboardResponse response = new HighlightsDashboardResponse(
                TimelineRange.LAST_MONTH,
                highlights
        );

        when(playerHighlightsService.getHighlights(TimelineRange.LAST_MONTH)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/analytics/highlights/{range}", "LAST_MONTH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.highlights.BEST_SERVE.playerId", is(playerId)))
                .andExpect(jsonPath("$.highlights.BEST_SERVE.playerName", is("Dominant Player")))
                .andExpect(jsonPath("$.highlights.BEST_NET_PLAYER.playerId", is(playerId)))
                .andExpect(jsonPath("$.highlights.BEST_NET_PLAYER.playerName", is("Dominant Player")))
                .andExpect(jsonPath("$.highlights.BEST_PRESSURE_PLAYER.playerId", is(playerId)))
                .andExpect(jsonPath("$.highlights.BEST_PRESSURE_PLAYER.playerName", is("Dominant Player")));

        verify(playerHighlightsService).getHighlights(TimelineRange.LAST_MONTH);
    }
}