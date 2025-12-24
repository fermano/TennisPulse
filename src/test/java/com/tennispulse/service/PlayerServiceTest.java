package com.tennispulse.service;

import com.tennispulse.domain.Handedness;
import com.tennispulse.domain.PlayerEntity;
import com.tennispulse.repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerService playerService;

    @Test
    void create_shouldSavePlayerAndReturnCreated() {
        PlayerEntity input = PlayerEntity.builder()
                .id(UUID.randomUUID().toString())
                .name("Alice")
                .handedness(Handedness.RIGHT)
                .build();

        PlayerEntity saved = PlayerEntity.builder()
                .id(UUID.randomUUID().toString())
                .name("Alice")
                .handedness(Handedness.RIGHT)
                .build();

        when(playerRepository.save(any(PlayerEntity.class))).thenReturn(saved);

        PlayerEntity result = playerService.create(input);

        assertNotNull(result);
        assertEquals(saved.getId(), result.getId());
        assertEquals("Alice", result.getName());

        ArgumentCaptor<PlayerEntity> captor = ArgumentCaptor.forClass(PlayerEntity.class);
        verify(playerRepository).save(captor.capture());
        assertNull(captor.getValue().getId(), "ID should be null before save");
    }

    @Test
    void findAll_shouldReturnNonDeletedPlayers() {
        PlayerEntity p1 = PlayerEntity.builder().id(UUID.randomUUID().toString()).name("A").build();
        PlayerEntity p2 = PlayerEntity.builder().id(UUID.randomUUID().toString()).name("B").build();

        when(playerRepository.findByDeletedFalse()).thenReturn(List.of(p1, p2));

        List<PlayerEntity> result = playerService.findAll();

        assertEquals(2, result.size());
        verify(playerRepository).findByDeletedFalse();
    }

    @Test
    void findById_shouldReturnPlayerWhenExists() {
        String id = UUID.randomUUID().toString();
        PlayerEntity p = PlayerEntity.builder().id(id).name("Alice").build();

        when(playerRepository.findById(id)).thenReturn(Optional.of(p));

        PlayerEntity result = playerService.findById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        String id = UUID.randomUUID().toString();
        when(playerRepository.findById(id)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> playerService.findById(id));

        assertTrue(ex.getMessage().contains("Player not found"));
    }

    @Test
    void update_shouldUpdateFieldsAndSave() {
        String id = UUID.randomUUID().toString();
        PlayerEntity existing = PlayerEntity.builder()
                .id(id)
                .name("Old Name")
                .handedness(Handedness.LEFT)
                .build();

        PlayerEntity updated = PlayerEntity.builder()
                .name("New Name")
                .handedness(Handedness.RIGHT)
                .build();

        when(playerRepository.findById(id)).thenReturn(Optional.of(existing));
        when(playerRepository.save(any(PlayerEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlayerEntity result = playerService.update(id, updated);

        assertEquals("New Name", result.getName());
        assertEquals(Handedness.RIGHT, result.getHandedness());
        verify(playerRepository).save(existing);
    }

    @Test
    void delete_shouldSoftDeleteWhenNotDeleted() {
        String id = UUID.randomUUID().toString();
        PlayerEntity existing = PlayerEntity.builder()
                .id(id)
                .name("Alice")
                .handedness(Handedness.RIGHT)
                .deleted(false)
                .build();

        when(playerRepository.findById(id)).thenReturn(Optional.of(existing));

        playerService.delete(id);

        assertTrue(existing.isDeleted());
        assertNull(existing.getHandedness());
        assertNotNull(existing.getDeletedAt());
        assertTrue(existing.getName().startsWith("Deleted player"));

        verify(playerRepository).save(existing);
    }

    @Test
    void delete_shouldNotSaveWhenAlreadyDeleted() {
        String id = UUID.randomUUID().toString();
        PlayerEntity existing = PlayerEntity.builder()
                .id(id)
                .name("Already deleted")
                .deleted(true)
                .deletedAt(Instant.now())
                .build();

        when(playerRepository.findById(id)).thenReturn(Optional.of(existing));

        playerService.delete(id);

        verify(playerRepository, never()).save(any());
    }
}
