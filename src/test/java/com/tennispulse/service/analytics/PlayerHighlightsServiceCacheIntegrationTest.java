package com.tennispulse.service.analytics;

import com.tennispulse.api.dto.PlayerWinsRankingDto;
import com.tennispulse.repository.MatchRepository;
import com.tennispulse.service.RankingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.cache.type=redis",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379"
})
class RankingServiceCacheIntegrationTest {

    @Autowired
    private RankingService rankingService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @MockBean
    private MatchRepository matchRepository;


    @BeforeEach
    void setUp() {
        RedisConnection connection = redisConnectionFactory.getConnection();
        connection.serverCommands().flushAll();
        connection.close();

        cacheManager.getCacheNames().forEach(cacheName ->
                Objects.requireNonNull(cacheManager.getCache(cacheName)).clear()
        );

        clearInvocations(matchRepository);
    }

    @Test
    void getTopWinnersCurrentYear_shouldCacheAndOnlyQueryDatabaseOnce() {
        // Arrange
        String player1Id = UUID.randomUUID().toString();
        String player2Id = UUID.randomUUID().toString();

        List<Object[]> mockResults = Arrays.asList(
                new Object[]{player1Id, "Rafael Nadal", 25L},
                new Object[]{player2Id, "Roger Federer", 22L}
        );

        when(matchRepository.findWinCountsBetween(any(Instant.class), any(Instant.class)))
                .thenReturn(mockResults);

        // Act - Call 3 times with same parameters
        List<PlayerWinsRankingDto> firstCall = rankingService.getTopWinnersCurrentYear(10);
        List<PlayerWinsRankingDto> secondCall = rankingService.getTopWinnersCurrentYear(10);
        List<PlayerWinsRankingDto> thirdCall = rankingService.getTopWinnersCurrentYear(10);

        // Assert - All calls return same data
        assertEquals(2, firstCall.size());
        assertEquals(2, secondCall.size());
        assertEquals(2, thirdCall.size());
        assertEquals("Rafael Nadal", firstCall.getFirst().playerName());

        // ✅ KEY ASSERTION: Repository called only ONCE (cached after first call)
        verify(matchRepository, times(1)).findWinCountsBetween(any(Instant.class), any(Instant.class));
    }

    @Test
    void getTopWinnersCurrentYear_withDifferentLimits_shouldCacheSeparately() {
        // Arrange
        List<Object[]> mockResults = Arrays.asList(
                new Object[]{UUID.randomUUID().toString(), "Player 1", 25L},
                new Object[]{UUID.randomUUID().toString(), "Player 2", 22L},
                new Object[]{UUID.randomUUID().toString(), "Player 3", 20L}
        );

        when(matchRepository.findWinCountsBetween(any(Instant.class), any(Instant.class)))
                .thenReturn(mockResults);

        // Act - Different limits = different cache keys
        rankingService.getTopWinnersCurrentYear(5);
        rankingService.getTopWinnersCurrentYear(10);
        rankingService.getTopWinnersCurrentYear(5);  // Should be cached
        rankingService.getTopWinnersCurrentYear(10); // Should be cached

        // Assert - Repository called TWICE (once per unique limit)
        verify(matchRepository, times(2)).findWinCountsBetween(any(Instant.class), any(Instant.class));
    }

    @Test
    void invalidateRankingsCache_shouldClearCacheAndQueryDatabaseAgain() {
        // Arrange
        List<Object[]> mockResults = Collections.singletonList(
                new Object[]{UUID.randomUUID().toString(), "Rafael Nadal", 25L}
        );

        when(matchRepository.findWinCountsBetween(any(Instant.class), any(Instant.class)))
                .thenReturn(mockResults);

        // Act - First call (cache miss)
        rankingService.getTopWinnersCurrentYear(10);
        verify(matchRepository, times(1)).findWinCountsBetween(any(), any());

        // Second call (cache hit)
        rankingService.getTopWinnersCurrentYear(10);
        verify(matchRepository, times(1)).findWinCountsBetween(any(), any()); // Still 1

        // Invalidate cache
        rankingService.invalidateRankingsCache();

        // Third call (cache miss again after invalidation)
        rankingService.getTopWinnersCurrentYear(10);

        // Assert - Repository called TWICE (once before eviction, once after)
        verify(matchRepository, times(2)).findWinCountsBetween(any(Instant.class), any(Instant.class));
    }

    @Test
    void getTopWinnersCurrentYear_shouldStoreInRedisCache() {
        // Arrange
        List<Object[]> mockResults = Collections.singletonList(
                new Object[]{UUID.randomUUID().toString(), "Rafael Nadal", 25L}
        );

        when(matchRepository.findWinCountsBetween(any(Instant.class), any(Instant.class)))
                .thenReturn(mockResults);

        // Act
        rankingService.getTopWinnersCurrentYear(10);
        rankingService.getTopWinnersCurrentYear(10); // Second call should be cached

        // Assert - Repository called only ONCE (proves caching works)
        verify(matchRepository, times(1)).findWinCountsBetween(any(Instant.class), any(Instant.class));
    }

    @Test
    void getTopWinnersLastMonth_shouldCacheSeparatelyFromCurrentYear() {
        // Arrange
        List<Object[]> mockResults = Collections.singletonList(
                new Object[]{UUID.randomUUID().toString(), "Rafael Nadal", 25L}
        );

        when(matchRepository.findWinCountsBetween(any(Instant.class), any(Instant.class)))
                .thenReturn(mockResults);

        // Act
        rankingService.getTopWinnersCurrentYear(10);  // Cache key: "current-year:10"
        rankingService.getTopWinnersLastMonth(10);    // Cache key: "last-month:10"
        rankingService.getTopWinnersCurrentYear(10);  // From cache
        rankingService.getTopWinnersLastMonth(10);    // From cache

        // Assert - Repository called TWICE (one per method)
        verify(matchRepository, times(2)).findWinCountsBetween(any(Instant.class), any(Instant.class));
    }

    @Test
    void invalidateRankingsCache_shouldClearAllRankingsCacheEntries() {
        // Arrange
        List<Object[]> mockResults = Collections.singletonList(
                new Object[]{UUID.randomUUID().toString(), "Rafael Nadal", 25L}
        );

        when(matchRepository.findWinCountsBetween(any(Instant.class), any(Instant.class)))
                .thenReturn(mockResults);

        // Populate multiple cache entries
        rankingService.getTopWinnersCurrentYear(5);
        rankingService.getTopWinnersCurrentYear(10);
        rankingService.getTopWinnersLastMonth(5);
        rankingService.getTopWinnersLastMonth(10);

        verify(matchRepository, times(4)).findWinCountsBetween(any(), any());

        // Act - Invalidate all rankings cache
        rankingService.invalidateRankingsCache();

        // All calls should hit DB again (cache cleared)
        rankingService.getTopWinnersCurrentYear(5);
        rankingService.getTopWinnersCurrentYear(10);
        rankingService.getTopWinnersLastMonth(5);
        rankingService.getTopWinnersLastMonth(10);

        // Assert - Repository called 8 times total (4 before + 4 after eviction)
        verify(matchRepository, times(8)).findWinCountsBetween(any(Instant.class), any(Instant.class));
    }
}