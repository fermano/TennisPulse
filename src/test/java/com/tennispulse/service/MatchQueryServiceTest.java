package com.tennispulse.service;

import com.tennispulse.api.controllers.MatchController;
import com.tennispulse.domain.ClubEntity;
import com.tennispulse.domain.MatchEntity;
import com.tennispulse.domain.MatchStatus;
import com.tennispulse.domain.PlayerEntity;
import com.tennispulse.repository.MatchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchQueryServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @InjectMocks
    private MatchQueryService matchQueryService;

    @Test
    void listAll_shouldMapEntitiesToResponses() {
        String matchId = UUID.randomUUID().toString();
        String clubId = UUID.randomUUID().toString();
        String p1Id = UUID.randomUUID().toString();
        String p2Id = UUID.randomUUID().toString();
        String winnerId = p1Id;

        ClubEntity club = ClubEntity.builder()
                .id(clubId)
                .name("Green Valley")
                .build();

        PlayerEntity p1 = PlayerEntity.builder()
                .id(p1Id)
                .name("Alice")
                .build();

        PlayerEntity p2 = PlayerEntity.builder()
                .id(p2Id)
                .name("Bruno")
                .build();

        MatchEntity match = MatchEntity.builder()
                .id(matchId)
                .club(club)
                .player1(p1)
                .player2(p2)
                .winner(p1)
                .finalScore("6-4 6-3")
                .status(MatchStatus.COMPLETED)
                .startTime(Instant.now().minusSeconds(3600))
                .endTime(Instant.now())
                .build();

        when(matchRepository.findAll()).thenReturn(List.of(match));

        List<MatchController.MatchResponse> result = matchQueryService.listAll();

        assertEquals(1, result.size());
        MatchController.MatchResponse r = result.get(0);

        assertEquals(matchId, r.id());
        assertEquals(clubId, r.clubId());
        assertEquals("Green Valley", r.clubName());
        assertEquals(p1Id, r.player1Id());
        assertEquals("Alice", r.player1Name());
        assertEquals(p2Id, r.player2Id());
        assertEquals("Bruno", r.player2Name());
        assertEquals(winnerId, r.winnerId());
        assertEquals("Alice", r.winnerName());
        assertEquals("6-4 6-3", r.finalScore());
        assertEquals(MatchStatus.COMPLETED, r.status());

        verify(matchRepository).findAll();
    }

    @Test
    void listAll_shouldHandleNullClubAndNullWinner() {
        String matchId = UUID.randomUUID().toString();
        String p1Id = UUID.randomUUID().toString();
        String p2Id = UUID.randomUUID().toString();

        PlayerEntity p1 = PlayerEntity.builder()
                .id(p1Id)
                .name("Alice")
                .build();

        PlayerEntity p2 = PlayerEntity.builder()
                .id(p2Id)
                .name("Bruno")
                .build();

        MatchEntity match = MatchEntity.builder()
                .id(matchId)
                .club(null)
                .player1(p1)
                .player2(p2)
                .winner(null)
                .finalScore(null)
                .status(MatchStatus.SCHEDULED)
                .build();

        when(matchRepository.findAll()).thenReturn(List.of(match));

        List<MatchController.MatchResponse> result = matchQueryService.listAll();

        assertEquals(1, result.size());
        MatchController.MatchResponse r = result.get(0);

        assertEquals(matchId, r.id());
        assertNull(r.clubId());
        assertNull(r.clubName());
        assertNull(r.winnerId());
        assertNull(r.winnerName());
        assertNull(r.finalScore());
    }

    @Test
    void getById_shouldReturnMappedResponseWhenFound() {
        String matchId = UUID.randomUUID().toString();
        String p1Id = UUID.randomUUID().toString();
        String p2Id = UUID.randomUUID().toString();

        PlayerEntity p1 = PlayerEntity.builder()
                .id(p1Id)
                .name("Alice")
                .build();

        PlayerEntity p2 = PlayerEntity.builder()
                .id(p2Id)
                .name("Bruno")
                .build();

        MatchEntity match = MatchEntity.builder()
                .id(matchId)
                .player1(p1)
                .player2(p2)
                .status(MatchStatus.SCHEDULED)
                .build();

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

        MatchController.MatchResponse r = matchQueryService.getById(matchId);

        assertEquals(matchId, r.id());
        assertEquals(p1Id, r.player1Id());
        assertEquals(p2Id, r.player2Id());
        verify(matchRepository).findById(matchId);
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        String matchId = UUID.randomUUID().toString();
        when(matchRepository.findById(matchId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> matchQueryService.getById(matchId));

        assertTrue(ex.getMessage().contains("Match not found"));
    }
}
