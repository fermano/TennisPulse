package com.tennispulse.service;

import com.tennispulse.api.dto.PlayerWinsRankingDto;
import com.tennispulse.repository.MatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @InjectMocks
    private RankingService rankingService;

    private String player1Id;
    private String player2Id;
    private String player3Id;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID().toString();
        player2Id = UUID.randomUUID().toString();
        player3Id = UUID.randomUUID().toString();
    }

    @Test
    void getTopWinnersCurrentYear_shouldReturnTopPlayers_whenDataExists() {
        // Arrange
        List<Object[]> mockResults = Arrays.asList(
                new Object[]{player1Id, "Rafael Nadal", 25L},
                new Object[]{player2Id, "Roger Federer", 22L},
                new Object[]{player3Id, "Novak Djokovic", 20L}
        );

        when(matchRepository.findWinCountsBetween(any(Instant.class), any(Instant.class)))
                .thenReturn(mockResults);

        // Act
        List<PlayerWinsRankingDto> result = rankingService.getTopWinnersCurrentYear(10);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());

        PlayerWinsRankingDto first = result.get(0);
        assertEquals(player1Id, first.playerId());
        assertEquals("Rafael Nadal", first.playerName());
        assertEquals(25L, first.wins());

        PlayerWinsRankingDto second = result.get(1);
        assertEquals(player2Id, second.playerId());
        assertEquals("Roger Federer", second.playerName());
        assertEquals(22L, second.wins());

        PlayerWinsRankingDto third = result.get(2);
        assertEquals(player3Id, third.playerId());
        assertEquals("Novak Djokovic", third.playerName());
        assertEquals(20L, third.wins());

        // Verify repository was called with correct date range
        ArgumentCaptor<Instant> fromCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> toCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(matchRepository).findWinCountsBetween(fromCaptor.capture(), toCaptor.capture());

        // Verify the 'from' date is at the start of the current year
        Instant from = fromCaptor.getValue();
        assertNotNull(from);

        // Verify the 'to' date is recent (within last few seconds)
        Instant to = toCaptor.getValue();
        assertNotNull(to);
        assertTrue(to.isAfter(from));
    }

    @Test
    void getTopWinnersCurrentYear_shouldRespectLimit_whenMorePlayersExist() {
        // Arrange
        List<Object[]> mockResults = Arrays.asList(
                new Object[]{player1Id, "Player 1", 25L},
                new Object[]{player2Id, "Player 2", 22L},
                new Object[]{player3Id, "Player 3", 20L},
                new Object[]{UUID.randomUUID().toString(), "Player 4", 18L},
                new Object[]{UUID.randomUUID().toString(), "Player 5", 15L}
        );

        when(matchRepository.findWinCountsBetween(any(Instant.class), any(Instant.class)))
                .thenReturn(mockResults);

        // Act
        List<PlayerWinsRankingDto> result = rankingService.getTopWinnersCurrentYear(3);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size()); // Should limit to 3
        assertEquals("Player 1", result.get(0).playerName());
        assertEquals("Player 2", result.get(1).playerName());
        assertEquals("Player 3", result.get(2).playerName());
    }

    @Test
    void getTopWinnersCurrentYear_shouldReturnEmptyList_whenNoDataExists() {
        // Arrange
        when(matchRepository.findWinCountsBetween(any(Instant.class), any(Instant.class)))
                .thenReturn(Collections.emptyList());

        // Act
        List<PlayerWinsRankingDto> result = rankingService.getTopWinnersCurrentYear(10);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(matchRepository).findWinCountsBetween(any(Instant.class), any(Instant.class));
    }

    @Test
    void getTopWinnersLastMonth_shouldReturnTopPlayers_whenDataExists() {
        // Arrange
        List<Object[]> mockResults = Arrays.asList(
                new Object[]{player1Id, "Rafael Nadal", 8L},
                new Object[]{player2Id, "Roger Federer", 7L},
                new Object[]{player3Id, "Novak Djokovic", 6L}
        );

        when(matchRepository.findWinCountsBetween(any(Instant.class), any(Instant.class)))
                .thenReturn(mockResults);

        // Act
        List<PlayerWinsRankingDto> result = rankingService.getTopWinnersLastMonth(10);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());

        PlayerWinsRankingDto first = result.get(0);
        assertEquals(player1Id, first.playerId());
        assertEquals("Rafael Nadal", first.playerName());
        assertEquals(8L, first.wins());

        // Verify repository was called
        ArgumentCaptor<Instant> fromCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> toCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(matchRepository).findWinCountsBetween(fromCaptor.capture(), toCaptor.capture());

        // Verify the time range is approximately 30 days
        Instant from = fromCaptor.getValue();
        Instant to = toCaptor.getValue();
        assertNotNull(from);
        assertNotNull(to);
        assertTrue(to.isAfter(from));
    }

    @Test
    void getTopWinnersLastMonth_shouldRespectLimit_whenMorePlayersExist() {
        // Arrange
        List<Object[]> mockResults = Arrays.asList(
                new Object[]{player1Id, "Player 1", 10L},
                new Object[]{player2Id, "Player 2", 9L},
                new Object[]{player3Id, "Player 3", 8L},
                new Object[]{UUID.randomUUID().toString(), "Player 4", 7L},
                new Object[]{UUID.randomUUID().toString(), "Player 5", 6L},
                new Object[]{UUID.randomUUID().toString(), "Player 6", 5L}
        );

        when(matchRepository.findWinCountsBetween(any(Instant.class), any(Instant.class)))
                .thenReturn(mockResults);

        // Act
        List<PlayerWinsRankingDto> result = rankingService.getTopWinnersLastMonth(5);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.size()); // Should limit to 5
        assertEquals(10L, result.get(0).wins());
        assertEquals(9L, result.get(1).wins());
        assertEquals(8L, result.get(2).wins());
        assertEquals(7L, result.get(3).wins());
        assertEquals(6L, result.get(4).wins());
    }

    @Test
    void getTopWinnersLastMonth_shouldReturnEmptyList_whenNoDataExists() {
        // Arrange
        when(matchRepository.findWinCountsBetween(any(Instant.class), any(Instant.class)))
                .thenReturn(Collections.emptyList());

        // Act
        List<PlayerWinsRankingDto> result = rankingService.getTopWinnersLastMonth(10);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(matchRepository).findWinCountsBetween(any(Instant.class), any(Instant.class));
    }

    @Test
    void getTopWinnersCurrentYear_shouldHandleSinglePlayer() {
        // Arrange
        List<Object[]> mockResults = Collections.singletonList(
                new Object[]{player1Id, "Rafael Nadal", 15L}
        );

        when(matchRepository.findWinCountsBetween(any(Instant.class), any(Instant.class)))
                .thenReturn(mockResults);

        // Act
        List<PlayerWinsRankingDto> result = rankingService.getTopWinnersCurrentYear(10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(player1Id, result.get(0).playerId());
        assertEquals("Rafael Nadal", result.get(0).playerName());
        assertEquals(15L, result.get(0).wins());
    }

    @Test
    void getTopWinnersLastMonth_shouldHandleZeroLimit() {
        // Arrange
        List<Object[]> mockResults = Arrays.asList(
                new Object[]{player1Id, "Player 1", 10L},
                new Object[]{player2Id, "Player 2", 9L}
        );

        when(matchRepository.findWinCountsBetween(any(Instant.class), any(Instant.class)))
                .thenReturn(mockResults);

        // Act
        List<PlayerWinsRankingDto> result = rankingService.getTopWinnersLastMonth(0);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty()); // limit(0) should return empty list
    }

    @Test
    void getTopWinnersCurrentYear_shouldHandleLimitLargerThanResults() {
        // Arrange
        List<Object[]> mockResults = Arrays.asList(
                new Object[]{player1Id, "Player 1", 25L},
                new Object[]{player2Id, "Player 2", 22L}
        );

        when(matchRepository.findWinCountsBetween(any(Instant.class), any(Instant.class)))
                .thenReturn(mockResults);

        // Act
        List<PlayerWinsRankingDto> result = rankingService.getTopWinnersCurrentYear(100);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size()); // Should return all available results
        assertEquals("Player 1", result.get(0).playerName());
        assertEquals("Player 2", result.get(1).playerName());
    }

    @Test
    void getTopWinnersCurrentYear_shouldHandlePlayersWithSameWinCount() {
        // Arrange
        List<Object[]> mockResults = Arrays.asList(
                new Object[]{player1Id, "Player 1", 20L},
                new Object[]{player2Id, "Player 2", 20L},
                new Object[]{player3Id, "Player 3", 20L}
        );

        when(matchRepository.findWinCountsBetween(any(Instant.class), any(Instant.class)))
                .thenReturn(mockResults);

        // Act
        List<PlayerWinsRankingDto> result = rankingService.getTopWinnersCurrentYear(10);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        // All should have same win count
        result.forEach(ranking -> assertEquals(20L, ranking.wins()));
    }

    @Test
    void getTopWinnersLastMonth_shouldHandleLimitOfOne() {
        // Arrange
        List<Object[]> mockResults = Arrays.asList(
                new Object[]{player1Id, "Rafael Nadal", 10L},
                new Object[]{player2Id, "Roger Federer", 9L},
                new Object[]{player3Id, "Novak Djokovic", 8L}
        );

        when(matchRepository.findWinCountsBetween(any(Instant.class), any(Instant.class)))
                .thenReturn(mockResults);

        // Act
        List<PlayerWinsRankingDto> result = rankingService.getTopWinnersLastMonth(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Rafael Nadal", result.get(0).playerName());
        assertEquals(10L, result.get(0).wins());
    }
}