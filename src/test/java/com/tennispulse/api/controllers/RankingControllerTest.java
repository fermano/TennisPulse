package com.tennispulse.api.controllers;

import com.tennispulse.api.dto.PlayerWinsRankingDto;
import com.tennispulse.service.RankingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(RankingController.class)
class RankingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RankingService rankingService;

    @Test
    void winsCurrentYear_shouldReturnTopWinners_withDefaultLimit() throws Exception {
        // Arrange
        String player1Id = UUID.randomUUID().toString();
        String player2Id = UUID.randomUUID().toString();
        String player3Id = UUID.randomUUID().toString();

        List<PlayerWinsRankingDto> rankings = Arrays.asList(
                new PlayerWinsRankingDto(player1Id, "Rafael Nadal", 25L),
                new PlayerWinsRankingDto(player2Id, "Roger Federer", 22L),
                new PlayerWinsRankingDto(player3Id, "Novak Djokovic", 20L)
        );

        when(rankingService.getTopWinnersCurrentYear(10)).thenReturn(rankings);

        // Act & Assert
        mockMvc.perform(get("/api/rankings/wins/current-year"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].playerId", is(player1Id)))
                .andExpect(jsonPath("$[0].playerName", is("Rafael Nadal")))
                .andExpect(jsonPath("$[0].wins", is(25)))
                .andExpect(jsonPath("$[1].playerId", is(player2Id)))
                .andExpect(jsonPath("$[1].playerName", is("Roger Federer")))
                .andExpect(jsonPath("$[1].wins", is(22)))
                .andExpect(jsonPath("$[2].playerId", is(player3Id)))
                .andExpect(jsonPath("$[2].playerName", is("Novak Djokovic")))
                .andExpect(jsonPath("$[2].wins", is(20)));

        verify(rankingService).getTopWinnersCurrentYear(10);
    }

    @Test
    void winsCurrentYear_shouldReturnTopWinners_withCustomLimit() throws Exception {
        // Arrange
        String player1Id = UUID.randomUUID().toString();
        String player2Id = UUID.randomUUID().toString();

        List<PlayerWinsRankingDto> rankings = Arrays.asList(
                new PlayerWinsRankingDto(player1Id, "Rafael Nadal", 25L),
                new PlayerWinsRankingDto(player2Id, "Roger Federer", 22L)
        );

        when(rankingService.getTopWinnersCurrentYear(5)).thenReturn(rankings);

        // Act & Assert
        mockMvc.perform(get("/api/rankings/wins/current-year")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].playerId", is(player1Id)))
                .andExpect(jsonPath("$[0].playerName", is("Rafael Nadal")))
                .andExpect(jsonPath("$[0].wins", is(25)))
                .andExpect(jsonPath("$[1].playerId", is(player2Id)))
                .andExpect(jsonPath("$[1].playerName", is("Roger Federer")))
                .andExpect(jsonPath("$[1].wins", is(22)));

        verify(rankingService).getTopWinnersCurrentYear(5);
    }

    @Test
    void winsCurrentYear_shouldReturnEmptyList_whenNoRankingsExist() throws Exception {
        // Arrange
        when(rankingService.getTopWinnersCurrentYear(10)).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/rankings/wins/current-year"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(rankingService).getTopWinnersCurrentYear(10);
    }

    @Test
    void winsCurrentYear_shouldHandleLimitOfOne() throws Exception {
        // Arrange
        String playerId = UUID.randomUUID().toString();
        List<PlayerWinsRankingDto> rankings = Collections.singletonList(
                new PlayerWinsRankingDto(playerId, "Rafael Nadal", 25L)
        );

        when(rankingService.getTopWinnersCurrentYear(1)).thenReturn(rankings);

        // Act & Assert
        mockMvc.perform(get("/api/rankings/wins/current-year")
                        .param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].playerId", is(playerId)))
                .andExpect(jsonPath("$[0].playerName", is("Rafael Nadal")))
                .andExpect(jsonPath("$[0].wins", is(25)));

        verify(rankingService).getTopWinnersCurrentYear(1);
    }

    @Test
    void winsCurrentYear_shouldHandleLargeLimit() throws Exception {
        // Arrange
        String player1Id = UUID.randomUUID().toString();
        String player2Id = UUID.randomUUID().toString();

        List<PlayerWinsRankingDto> rankings = Arrays.asList(
                new PlayerWinsRankingDto(player1Id, "Player 1", 30L),
                new PlayerWinsRankingDto(player2Id, "Player 2", 28L)
        );

        when(rankingService.getTopWinnersCurrentYear(100)).thenReturn(rankings);

        // Act & Assert
        mockMvc.perform(get("/api/rankings/wins/current-year")
                        .param("limit", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(rankingService).getTopWinnersCurrentYear(100);
    }

    @Test
    void winsLastMonth_shouldReturnTopWinners_withDefaultLimit() throws Exception {
        // Arrange
        String player1Id = UUID.randomUUID().toString();
        String player2Id = UUID.randomUUID().toString();
        String player3Id = UUID.randomUUID().toString();

        List<PlayerWinsRankingDto> rankings = Arrays.asList(
                new PlayerWinsRankingDto(player1Id, "Rafael Nadal", 8L),
                new PlayerWinsRankingDto(player2Id, "Roger Federer", 7L),
                new PlayerWinsRankingDto(player3Id, "Novak Djokovic", 6L)
        );

        when(rankingService.getTopWinnersLastMonth(10)).thenReturn(rankings);

        // Act & Assert
        mockMvc.perform(get("/api/rankings/wins/last-month"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].playerId", is(player1Id)))
                .andExpect(jsonPath("$[0].playerName", is("Rafael Nadal")))
                .andExpect(jsonPath("$[0].wins", is(8)))
                .andExpect(jsonPath("$[1].playerId", is(player2Id)))
                .andExpect(jsonPath("$[1].playerName", is("Roger Federer")))
                .andExpect(jsonPath("$[1].wins", is(7)))
                .andExpect(jsonPath("$[2].playerId", is(player3Id)))
                .andExpect(jsonPath("$[2].playerName", is("Novak Djokovic")))
                .andExpect(jsonPath("$[2].wins", is(6)));

        verify(rankingService).getTopWinnersLastMonth(10);
    }

    @Test
    void winsLastMonth_shouldReturnTopWinners_withCustomLimit() throws Exception {
        // Arrange
        String player1Id = UUID.randomUUID().toString();
        String player2Id = UUID.randomUUID().toString();
        String player3Id = UUID.randomUUID().toString();

        List<PlayerWinsRankingDto> rankings = Arrays.asList(
                new PlayerWinsRankingDto(player1Id, "Player 1", 10L),
                new PlayerWinsRankingDto(player2Id, "Player 2", 9L),
                new PlayerWinsRankingDto(player3Id, "Player 3", 8L)
        );

        when(rankingService.getTopWinnersLastMonth(3)).thenReturn(rankings);

        // Act & Assert
        mockMvc.perform(get("/api/rankings/wins/last-month")
                        .param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].wins", is(10)))
                .andExpect(jsonPath("$[1].wins", is(9)))
                .andExpect(jsonPath("$[2].wins", is(8)));

        verify(rankingService).getTopWinnersLastMonth(3);
    }

    @Test
    void winsLastMonth_shouldReturnEmptyList_whenNoRankingsExist() throws Exception {
        // Arrange
        when(rankingService.getTopWinnersLastMonth(10)).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/rankings/wins/last-month"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(rankingService).getTopWinnersLastMonth(10);
    }

    @Test
    void winsLastMonth_shouldHandleLimitOfOne() throws Exception {
        // Arrange
        String playerId = UUID.randomUUID().toString();
        List<PlayerWinsRankingDto> rankings = Collections.singletonList(
                new PlayerWinsRankingDto(playerId, "Rafael Nadal", 8L)
        );

        when(rankingService.getTopWinnersLastMonth(1)).thenReturn(rankings);

        // Act & Assert
        mockMvc.perform(get("/api/rankings/wins/last-month")
                        .param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].playerId", is(playerId)))
                .andExpect(jsonPath("$[0].wins", is(8)));

        verify(rankingService).getTopWinnersLastMonth(1);
    }

    @Test
    void winsLastMonth_shouldHandleZeroWins() throws Exception {
        // Arrange
        String playerId = UUID.randomUUID().toString();
        List<PlayerWinsRankingDto> rankings = Collections.singletonList(
                new PlayerWinsRankingDto(playerId, "New Player", 0L)
        );

        when(rankingService.getTopWinnersLastMonth(10)).thenReturn(rankings);

        // Act & Assert
        mockMvc.perform(get("/api/rankings/wins/last-month"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].wins", is(0)));

        verify(rankingService).getTopWinnersLastMonth(10);
    }

    @Test
    void winsCurrentYear_shouldHandlePlayersWithSameWinCount() throws Exception {
        // Arrange
        String player1Id = UUID.randomUUID().toString();
        String player2Id = UUID.randomUUID().toString();
        String player3Id = UUID.randomUUID().toString();

        List<PlayerWinsRankingDto> rankings = Arrays.asList(
                new PlayerWinsRankingDto(player1Id, "Player 1", 20L),
                new PlayerWinsRankingDto(player2Id, "Player 2", 20L),
                new PlayerWinsRankingDto(player3Id, "Player 3", 20L)
        );

        when(rankingService.getTopWinnersCurrentYear(10)).thenReturn(rankings);

        // Act & Assert
        mockMvc.perform(get("/api/rankings/wins/current-year"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].wins", is(20)))
                .andExpect(jsonPath("$[1].wins", is(20)))
                .andExpect(jsonPath("$[2].wins", is(20)));

        verify(rankingService).getTopWinnersCurrentYear(10);
    }

    @Test
    void winsLastMonth_shouldHandlePlayersWithSameWinCount() throws Exception {
        // Arrange
        String player1Id = UUID.randomUUID().toString();
        String player2Id = UUID.randomUUID().toString();

        List<PlayerWinsRankingDto> rankings = Arrays.asList(
                new PlayerWinsRankingDto(player1Id, "Player 1", 15L),
                new PlayerWinsRankingDto(player2Id, "Player 2", 15L)
        );

        when(rankingService.getTopWinnersLastMonth(10)).thenReturn(rankings);

        // Act & Assert
        mockMvc.perform(get("/api/rankings/wins/last-month"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].wins", is(15)))
                .andExpect(jsonPath("$[1].wins", is(15)));

        verify(rankingService).getTopWinnersLastMonth(10);
    }
}