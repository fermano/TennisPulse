package com.tennispulse.service;

import com.tennispulse.api.MatchController;
import com.tennispulse.domain.ClubEntity;
import com.tennispulse.domain.MatchEntity;
import com.tennispulse.domain.MatchStatus;
import com.tennispulse.domain.PlayerEntity;
import com.tennispulse.repository.ClubRepository;
import com.tennispulse.repository.MatchRepository;
import com.tennispulse.repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private SqsMatchEventPublisher matchEventPublisher;

    @InjectMocks
    private MatchService matchService;

    @Test
    void create_shouldCreateMatchWithScheduledStatus() {
        UUID clubId = UUID.randomUUID();
        UUID p1Id = UUID.randomUUID();
        UUID p2Id = UUID.randomUUID();

        ClubEntity club = ClubEntity.builder().id(clubId).name("Club").build();
        PlayerEntity p1 = PlayerEntity.builder().id(p1Id).name("P1").build();
        PlayerEntity p2 = PlayerEntity.builder().id(p2Id).name("P2").build();

        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
        when(playerRepository.findById(p1Id)).thenReturn(Optional.of(p1));
        when(playerRepository.findById(p2Id)).thenReturn(Optional.of(p2));

        MatchEntity saved = MatchEntity.builder()
                .id(UUID.randomUUID())
                .club(club)
                .player1(p1)
                .player2(p2)
                .status(MatchStatus.SCHEDULED)
                .build();

        when(matchRepository.save(any(MatchEntity.class))).thenReturn(saved);

        MatchEntity result = matchService.create(clubId, p1Id, p2Id);

        assertNotNull(result);
        assertEquals(MatchStatus.SCHEDULED, result.getStatus());
        verify(matchRepository).save(any(MatchEntity.class));
    }

    @Test
    void create_shouldThrowWhenClubNotFound() {
        UUID clubId = UUID.randomUUID();
        UUID p1Id = UUID.randomUUID();
        UUID p2Id = UUID.randomUUID();

        when(clubRepository.findById(clubId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> matchService.create(clubId, p1Id, p2Id));

        assertTrue(ex.getMessage().contains("Club not found"));
        verify(playerRepository, never()).findById(any());
        verify(matchRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenPlayer1NotFound() {
        UUID clubId = UUID.randomUUID();
        UUID p1Id = UUID.randomUUID();
        UUID p2Id = UUID.randomUUID();

        ClubEntity club = ClubEntity.builder().id(clubId).build();
        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
        when(playerRepository.findById(p1Id)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> matchService.create(clubId, p1Id, p2Id));

        assertTrue(ex.getMessage().contains("Player1 not found"));
        verify(matchRepository, never()).save(any());
    }

    @Test
    void findAll_shouldReturnAllMatches() {
        when(matchRepository.findAll()).thenReturn(List.of(
                MatchEntity.builder().id(UUID.randomUUID()).build(),
                MatchEntity.builder().id(UUID.randomUUID()).build()
        ));

        List<MatchEntity> result = matchService.findAll();

        assertEquals(2, result.size());
        verify(matchRepository).findAll();
    }

    @Test
    void findById_shouldReturnMatchWhenExists() {
        UUID id = UUID.randomUUID();
        MatchEntity match = MatchEntity.builder().id(id).build();
        when(matchRepository.findById(id)).thenReturn(Optional.of(match));

        MatchEntity result = matchService.findById(id);

        assertEquals(id, result.getId());
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(matchRepository.findById(id)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> matchService.findById(id));

        assertTrue(ex.getMessage().contains("Match not found"));
    }

    @Test
    void updateStatus_toInProgress_shouldSetStartTime() {
        UUID id = UUID.randomUUID();
        MatchEntity match = MatchEntity.builder()
                .id(id)
                .status(MatchStatus.SCHEDULED)
                .startTime(null)
                .build();

        MatchController.UpdateMatchStatusRequest request = new MatchController.UpdateMatchStatusRequest();
        request.setStatus(MatchStatus.IN_PROGRESS);

        when(matchRepository.findById(id)).thenReturn(Optional.of(match));
        when(matchRepository.save(any(MatchEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MatchEntity result = matchService.updateStatus(id, request);

        assertEquals(MatchStatus.IN_PROGRESS, result.getStatus());
        assertNotNull(result.getStartTime());
        verify(matchRepository).save(match);
    }

    @Test
    void updateStatus_toCompletedWithoutWinnerOrScore_shouldThrow() {
        UUID id = UUID.randomUUID();
        MatchEntity match = MatchEntity.builder()
                .id(id)
                .status(MatchStatus.IN_PROGRESS)
                .startTime(Instant.now())
                .build();

        when(matchRepository.findById(id)).thenReturn(Optional.of(match));

        MatchController.UpdateMatchStatusRequest request = new MatchController.UpdateMatchStatusRequest();
        request.setStatus(MatchStatus.COMPLETED);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> matchService.updateStatus(id, request));

        assertTrue(ex.getMessage().contains("Winner and finalScore are required"));
        verify(playerRepository, never()).findById(any());
        verify(matchRepository, never()).save(any());
    }

    @Test
    void updateStatus_toCompleted_shouldSetWinnerFinalScoreAndEndTime() {
        UUID id = UUID.randomUUID();
        UUID winnerId = UUID.randomUUID();
        MatchEntity match = MatchEntity.builder()
                .id(id)
                .status(MatchStatus.IN_PROGRESS)
                .startTime(Instant.now())
                .build();

        PlayerEntity winner = PlayerEntity.builder()
                .id(winnerId)
                .name("Winner")
                .build();

        when(matchRepository.findById(id)).thenReturn(Optional.of(match));
        when(playerRepository.findById(winnerId)).thenReturn(Optional.of(winner));
        when(matchRepository.save(any(MatchEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MatchController.UpdateMatchStatusRequest request = new MatchController.UpdateMatchStatusRequest();
        request.setWinnerId(winnerId);
        request.setStatus(MatchStatus.COMPLETED);
        request.setFinalScore("6-4 6-3");

        MatchEntity result = matchService.updateStatus(id, request);

        assertEquals(MatchStatus.COMPLETED, result.getStatus());
        assertEquals(winner, result.getWinner());
        assertEquals("6-4 6-3", result.getFinalScore());
        assertNotNull(result.getEndTime());
        verify(matchRepository).save(match);
    }

    @Test
    void updateStatus_toCompletedWithStats_shouldPublishMatchCompletedEvent() {
        UUID id = UUID.randomUUID();
        UUID winnerId = UUID.randomUUID();

        MatchEntity match = MatchEntity.builder()
                .id(id)
                .status(MatchStatus.IN_PROGRESS)
                .startTime(Instant.now())
                .build();

        PlayerEntity winner = PlayerEntity.builder()
                .id(winnerId)
                .name("Winner")
                .build();

        when(matchRepository.findById(id)).thenReturn(Optional.of(match));
        when(playerRepository.findById(winnerId)).thenReturn(Optional.of(winner));
        when(matchRepository.save(any(MatchEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // build player stats payload
        MatchController.PlayerStatsRequest playerStatsRequest = new MatchController.PlayerStatsRequest();
        playerStatsRequest.setPlayerId(winnerId);
        playerStatsRequest.setFirstServeIn(65.0);
        playerStatsRequest.setFirstServePointsWon(72.0);
        playerStatsRequest.setSecondServePointsWon(50.0);
        playerStatsRequest.setUnforcedErrorsForehand(5);
        playerStatsRequest.setUnforcedErrorsBackhand(7);
        playerStatsRequest.setWinners(20);
        playerStatsRequest.setBreakPointConversion(40.0);
        playerStatsRequest.setBreakPointsSaved(55.0);
        playerStatsRequest.setNetPointsWon(60.0);
        playerStatsRequest.setLongRallyWinRate(48.0);

        List<MatchController.PlayerStatsRequest> statsList = List.of(playerStatsRequest);

        MatchController.UpdateMatchStatusRequest request = new MatchController.UpdateMatchStatusRequest();
        request.setWinnerId(winnerId);
        request.setStatus(MatchStatus.COMPLETED);
        request.setFinalScore("6-4 6-3");
        request.setPlayerStats(statsList);

        // when
        MatchEntity result = matchService.updateStatus(id, request);

        // then: domain changes
        assertEquals(MatchStatus.COMPLETED, result.getStatus());
        assertEquals(winner, result.getWinner());
        assertEquals("6-4 6-3", result.getFinalScore());
        assertNotNull(result.getEndTime());
        verify(matchRepository).save(match);

        // and: event published with stats
        verify(matchEventPublisher).publishMatchCompleted(match, statsList);
    }


    @Test
    void updateStatus_toCancelled_shouldClearWinnerAndFinalScoreAndSetEndTime() {
        UUID id = UUID.randomUUID();
        MatchEntity match = MatchEntity.builder()
                .id(id)
                .status(MatchStatus.IN_PROGRESS)
                .startTime(Instant.now())
                .winner(PlayerEntity.builder().id(UUID.randomUUID()).build())
                .finalScore("6-4 6-3")
                .build();

        when(matchRepository.findById(id)).thenReturn(Optional.of(match));
        when(matchRepository.save(any(MatchEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MatchController.UpdateMatchStatusRequest request = new MatchController.UpdateMatchStatusRequest();
        request.setStatus(MatchStatus.CANCELLED);

        MatchEntity result = matchService.updateStatus(id, request);

        assertEquals(MatchStatus.CANCELLED, result.getStatus());
        assertNull(result.getWinner());
        assertNull(result.getFinalScore());
        assertNotNull(result.getEndTime());
        verify(matchRepository).save(match);
    }

    @Test
    void delete_shouldCallRepositoryDeleteById() {
        UUID id = UUID.randomUUID();

        matchService.delete(id);

        verify(matchRepository).deleteById(id);
    }
}