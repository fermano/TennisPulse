package com.tennispulse.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennispulse.domain.ClubEntity;
import com.tennispulse.service.ClubService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ClubController.class)
class ClubControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClubService clubService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_shouldReturnCreatedClub_withCreatedStatus() throws Exception {
        // Arrange
        String clubId = UUID.randomUUID().toString();
        ClubEntity inputClub = ClubEntity.builder()
                .name("Green Valley Tennis Club")
                .city("Austin")
                .country("USA")
                .build();

        ClubEntity createdClub = ClubEntity.builder()
                .id(clubId)
                .name("Green Valley Tennis Club")
                .city("Austin")
                .country("USA")
                .deleted(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(clubService.create(any(ClubEntity.class))).thenReturn(createdClub);

        // Act & Assert
        mockMvc.perform(post("/api/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputClub)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(clubId)))
                .andExpect(jsonPath("$.name", is("Green Valley Tennis Club")))
                .andExpect(jsonPath("$.city", is("Austin")))
                .andExpect(jsonPath("$.country", is("USA")))
                .andExpect(jsonPath("$.deleted", is(false)));

        verify(clubService).create(any(ClubEntity.class));
    }

    @Test
    void list_shouldReturnListOfClubs() throws Exception {
        // Arrange
        String club1Id = UUID.randomUUID().toString();
        String club2Id = UUID.randomUUID().toString();

        ClubEntity club1 = ClubEntity.builder()
                .id(club1Id)
                .name("Green Valley Tennis Club")
                .city("Austin")
                .country("USA")
                .deleted(false)
                .build();

        ClubEntity club2 = ClubEntity.builder()
                .id(club2Id)
                .name("River Oaks Country Club")
                .city("Houston")
                .country("USA")
                .deleted(false)
                .build();

        List<ClubEntity> clubs = Arrays.asList(club1, club2);

        when(clubService.findAll()).thenReturn(clubs);

        // Act & Assert
        mockMvc.perform(get("/api/clubs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(club1Id)))
                .andExpect(jsonPath("$[0].name", is("Green Valley Tennis Club")))
                .andExpect(jsonPath("$[0].city", is("Austin")))
                .andExpect(jsonPath("$[1].id", is(club2Id)))
                .andExpect(jsonPath("$[1].name", is("River Oaks Country Club")))
                .andExpect(jsonPath("$[1].city", is("Houston")));

        verify(clubService).findAll();
    }

    @Test
    void list_shouldReturnEmptyList_whenNoClubsExist() throws Exception {
        // Arrange
        when(clubService.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/clubs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(clubService).findAll();
    }

    @Test
    void get_shouldReturnSingleClub_whenClubExists() throws Exception {
        // Arrange
        String clubId = UUID.randomUUID().toString();
        ClubEntity club = ClubEntity.builder()
                .id(clubId)
                .name("Green Valley Tennis Club")
                .city("Austin")
                .country("USA")
                .deleted(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(clubService.findById(clubId)).thenReturn(club);

        // Act & Assert
        mockMvc.perform(get("/api/clubs/{id}", clubId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(clubId)))
                .andExpect(jsonPath("$.name", is("Green Valley Tennis Club")))
                .andExpect(jsonPath("$.city", is("Austin")))
                .andExpect(jsonPath("$.country", is("USA")));

        verify(clubService).findById(clubId);
    }

    @Test
    void get_shouldThrowException_whenClubNotFound() throws Exception {
        // Arrange
        String clubId = UUID.randomUUID().toString();

        when(clubService.findById(clubId))
                .thenThrow(new IllegalArgumentException("Club not found with id: " + clubId));

        // Act & Assert
        // Without @ControllerAdvice, this becomes a 500 error
        // We just verify the exception was thrown and service was called
        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(get("/api/clubs/{id}", clubId));
        });

        assertTrue(exception.getMessage().contains("Club not found"));
        verify(clubService).findById(clubId);
    }

    @Test
    void update_shouldReturnUpdatedClub() throws Exception {
        // Arrange
        String clubId = UUID.randomUUID().toString();

        ClubEntity updateRequest = ClubEntity.builder()
                .name("Updated Club Name")
                .city("Updated City")
                .country("Updated Country")
                .build();

        ClubEntity updatedClub = ClubEntity.builder()
                .id(clubId)
                .name("Updated Club Name")
                .city("Updated City")
                .country("Updated Country")
                .deleted(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(clubService.update(eq(clubId), any(ClubEntity.class))).thenReturn(updatedClub);

        // Act & Assert
        mockMvc.perform(put("/api/clubs/{id}", clubId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(clubId)))
                .andExpect(jsonPath("$.name", is("Updated Club Name")))
                .andExpect(jsonPath("$.city", is("Updated City")))
                .andExpect(jsonPath("$.country", is("Updated Country")));

        verify(clubService).update(eq(clubId), any(ClubEntity.class));
    }

    @Test
    void update_shouldThrowException_whenClubNotFound() throws Exception {
        // Arrange
        String clubId = UUID.randomUUID().toString();

        ClubEntity updateRequest = ClubEntity.builder()
                .name("Updated Club Name")
                .city("Updated City")
                .country("Updated Country")
                .build();

        when(clubService.update(eq(clubId), any(ClubEntity.class)))
                .thenThrow(new IllegalArgumentException("Club not found with id: " + clubId));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(put("/api/clubs/{id}", clubId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)));
        });

        assertTrue(exception.getMessage().contains("Club not found"));
        verify(clubService).update(eq(clubId), any(ClubEntity.class));
    }

    @Test
    void delete_shouldReturnNoContent_whenClubIsDeleted() throws Exception {
        // Arrange
        String clubId = UUID.randomUUID().toString();

        doNothing().when(clubService).delete(clubId);

        // Act & Assert
        mockMvc.perform(delete("/api/clubs/{id}", clubId))
                .andExpect(status().isNoContent());

        verify(clubService).delete(clubId);
    }

    @Test
    void delete_shouldThrowException_whenClubNotFound() throws Exception {
        // Arrange
        String clubId = UUID.randomUUID().toString();

        doThrow(new IllegalArgumentException("Club not found with id: " + clubId))
                .when(clubService).delete(clubId);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(delete("/api/clubs/{id}", clubId));
        });

        assertTrue(exception.getMessage().contains("Club not found"));
        verify(clubService).delete(clubId);
    }

    @Test
    void create_shouldHandleInvalidInput() throws Exception {
        // Arrange
        ClubEntity invalidClub = ClubEntity.builder()
                // Missing required fields
                .build();

        when(clubService.create(any(ClubEntity.class)))
                .thenThrow(new IllegalArgumentException("Invalid club data"));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(post("/api/clubs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidClub)));
        });

        assertTrue(exception.getMessage().contains("Invalid club data"));
    }

    @Test
    void update_shouldUpdateOnlyProvidedFields() throws Exception {
        // Arrange
        String clubId = UUID.randomUUID().toString();

        ClubEntity partialUpdate = ClubEntity.builder()
                .name("New Name Only")
                .build();

        ClubEntity updatedClub = ClubEntity.builder()
                .id(clubId)
                .name("New Name Only")
                .city("Original City")
                .country("Original Country")
                .deleted(false)
                .build();

        when(clubService.update(eq(clubId), any(ClubEntity.class))).thenReturn(updatedClub);

        // Act & Assert
        mockMvc.perform(put("/api/clubs/{id}", clubId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partialUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(clubId)))
                .andExpect(jsonPath("$.name", is("New Name Only")))
                .andExpect(jsonPath("$.city", is("Original City")))
                .andExpect(jsonPath("$.country", is("Original Country")));

        verify(clubService).update(eq(clubId), any(ClubEntity.class));
    }
}