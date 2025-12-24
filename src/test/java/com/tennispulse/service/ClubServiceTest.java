package com.tennispulse.service;

import com.tennispulse.domain.ClubEntity;
import com.tennispulse.repository.ClubRepository;
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
class ClubServiceTest {

    @Mock
    private ClubRepository clubRepository;

    @InjectMocks
    private ClubService clubService;

    @Test
    void create_shouldSaveClubAndReturnCreated() {
        ClubEntity input = ClubEntity.builder()
                .id(UUID.randomUUID().toString())
                .name("Green Valley")
                .city("Austin")
                .country("USA")
                .build();

        ClubEntity saved = ClubEntity.builder()
                .id(UUID.randomUUID().toString())
                .name("Green Valley")
                .city("Austin")
                .country("USA")
                .build();

        when(clubRepository.save(any(ClubEntity.class))).thenReturn(saved);

        ClubEntity result = clubService.create(input);

        assertNotNull(result);
        assertEquals(saved.getId(), result.getId());

        ArgumentCaptor<ClubEntity> captor = ArgumentCaptor.forClass(ClubEntity.class);
        verify(clubRepository).save(captor.capture());
        assertNull(captor.getValue().getId(), "ID should be null before save");
    }

    @Test
    void findAll_shouldReturnNonDeletedClubs() {
        ClubEntity c1 = ClubEntity.builder().id(UUID.randomUUID().toString()).name("A").build();
        ClubEntity c2 = ClubEntity.builder().id(UUID.randomUUID().toString()).name("B").build();

        when(clubRepository.findByDeletedFalse()).thenReturn(List.of(c1, c2));

        List<ClubEntity> result = clubService.findAll();

        assertEquals(2, result.size());
        verify(clubRepository).findByDeletedFalse();
    }

    @Test
    void findById_shouldReturnClubWhenExists() {
        String id = UUID.randomUUID().toString();
        ClubEntity c = ClubEntity.builder().id(id).name("Club").build();

        when(clubRepository.findById(id)).thenReturn(Optional.of(c));

        ClubEntity result = clubService.findById(id);

        assertEquals(id, result.getId());
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        String id = UUID.randomUUID().toString();
        when(clubRepository.findById(id)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> clubService.findById(id));

        assertTrue(ex.getMessage().contains("Club not found"));
    }

    @Test
    void update_shouldUpdateFieldsAndSave() {
        String id = UUID.randomUUID().toString();
        ClubEntity existing = ClubEntity.builder()
                .id(id)
                .name("Old")
                .city("Old City")
                .country("Old Country")
                .build();

        ClubEntity updated = ClubEntity.builder()
                .name("New")
                .city("New City")
                .country("New Country")
                .build();

        when(clubRepository.findById(id)).thenReturn(Optional.of(existing));
        when(clubRepository.save(any(ClubEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClubEntity result = clubService.update(id, updated);

        assertEquals("New", result.getName());
        assertEquals("New City", result.getCity());
        assertEquals("New Country", result.getCountry());
        verify(clubRepository).save(existing);
    }

    @Test
    void delete_shouldSoftDeleteWhenNotDeleted() {
        String id = UUID.randomUUID().toString();
        ClubEntity existing = ClubEntity.builder()
                .id(id)
                .name("Club")
                .city("City")
                .country("Country")
                .deleted(false)
                .build();

        when(clubRepository.findById(id)).thenReturn(Optional.of(existing));

        clubService.delete(id);

        assertTrue(existing.isDeleted());
        assertNull(existing.getCity());
        assertNull(existing.getCountry());
        assertNotNull(existing.getDeletedAt());
        assertTrue(existing.getName().startsWith("Deleted club"));

        verify(clubRepository).save(existing);
    }

    @Test
    void delete_shouldNotSaveWhenAlreadyDeleted() {
        String id = UUID.randomUUID().toString();
        ClubEntity existing = ClubEntity.builder()
                .id(id)
                .name("Deleted")
                .deleted(true)
                .deletedAt(Instant.now())
                .build();

        when(clubRepository.findById(id)).thenReturn(Optional.of(existing));

        clubService.delete(id);

        verify(clubRepository, never()).save(any());
    }
}