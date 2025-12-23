package com.tennispulse.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennispulse.domain.Handedness;
import com.tennispulse.domain.PlayerEntity;
import com.tennispulse.service.PlayerService;
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
@WebMvcTest(PlayerController.class)
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlayerService playerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_shouldReturnCreatedPlayer_withCreatedStatus() throws Exception {
        // Arrange
        String playerId = UUID.randomUUID().toString();
        PlayerEntity inputPlayer = PlayerEntity.builder()
                .name("Rafael Nadal")
                .handedness(Handedness.LEFT)
                .build();

        PlayerEntity createdPlayer = PlayerEntity.builder()
                .id(playerId)
                .name("Rafael Nadal")
                .handedness(Handedness.LEFT)
                .deleted(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(playerService.create(any(PlayerEntity.class))).thenReturn(createdPlayer);

        // Act & Assert
        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputPlayer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(playerId)))
                .andExpect(jsonPath("$.name", is("Rafael Nadal")))
                .andExpect(jsonPath("$.handedness", is("LEFT")))
                .andExpect(jsonPath("$.deleted", is(false)));

        verify(playerService).create(any(PlayerEntity.class));
    }

    @Test
    void list_shouldReturnListOfPlayers() throws Exception {
        // Arrange
        String player1Id = UUID.randomUUID().toString();
        String player2Id = UUID.randomUUID().toString();

        PlayerEntity player1 = PlayerEntity.builder()
                .id(player1Id)
                .name("Rafael Nadal")
                .handedness(Handedness.LEFT)
                .deleted(false)
                .build();

        PlayerEntity player2 = PlayerEntity.builder()
                .id(player2Id)
                .name("Roger Federer")
                .handedness(Handedness.RIGHT)
                .deleted(false)
                .build();

        List<PlayerEntity> players = Arrays.asList(player1, player2);

        when(playerService.findAll()).thenReturn(players);

        // Act & Assert
        mockMvc.perform(get("/api/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(player1Id)))
                .andExpect(jsonPath("$[0].name", is("Rafael Nadal")))
                .andExpect(jsonPath("$[0].handedness", is("LEFT")))
                .andExpect(jsonPath("$[1].id", is(player2Id)))
                .andExpect(jsonPath("$[1].name", is("Roger Federer")))
                .andExpect(jsonPath("$[1].handedness", is("RIGHT")));

        verify(playerService).findAll();
    }

    @Test
    void list_shouldReturnEmptyList_whenNoPlayersExist() throws Exception {
        // Arrange
        when(playerService.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(playerService).findAll();
    }

    @Test
    void get_shouldReturnSinglePlayer_whenPlayerExists() throws Exception {
        // Arrange
        String playerId = UUID.randomUUID().toString();
        PlayerEntity player = PlayerEntity.builder()
                .id(playerId)
                .name("Rafael Nadal")
                .handedness(Handedness.LEFT)
                .deleted(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(playerService.findById(playerId)).thenReturn(player);

        // Act & Assert
        mockMvc.perform(get("/api/players/{id}", playerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(playerId)))
                .andExpect(jsonPath("$.name", is("Rafael Nadal")))
                .andExpect(jsonPath("$.handedness", is("LEFT")));

        verify(playerService).findById(playerId);
    }

    @Test
    void get_shouldThrowException_whenPlayerNotFound() throws Exception {
        // Arrange
        String playerId = UUID.randomUUID().toString();

        when(playerService.findById(playerId))
                .thenThrow(new IllegalArgumentException("Player not found with id: " + playerId));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(get("/api/players/{id}", playerId));
        });

        assertTrue(exception.getMessage().contains("Player not found"));
        verify(playerService).findById(playerId);
    }

    @Test
    void update_shouldReturnUpdatedPlayer() throws Exception {
        // Arrange
        String playerId = UUID.randomUUID().toString();

        PlayerEntity updateRequest = PlayerEntity.builder()
                .name("Rafael Nadal Updated")
                .handedness(Handedness.LEFT)
                .build();

        PlayerEntity updatedPlayer = PlayerEntity.builder()
                .id(playerId)
                .name("Rafael Nadal Updated")
                .handedness(Handedness.LEFT)
                .deleted(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(playerService.update(eq(playerId), any(PlayerEntity.class))).thenReturn(updatedPlayer);

        // Act & Assert
        mockMvc.perform(put("/api/players/{id}", playerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(playerId)))
                .andExpect(jsonPath("$.name", is("Rafael Nadal Updated")))
                .andExpect(jsonPath("$.handedness", is("LEFT")));

        verify(playerService).update(eq(playerId), any(PlayerEntity.class));
    }

    @Test
    void update_shouldThrowException_whenPlayerNotFound() throws Exception {
        // Arrange
        String playerId = UUID.randomUUID().toString();

        PlayerEntity updateRequest = PlayerEntity.builder()
                .name("Updated Name")
                .handedness(Handedness.RIGHT)
                .build();

        when(playerService.update(eq(playerId), any(PlayerEntity.class)))
                .thenThrow(new IllegalArgumentException("Player not found with id: " + playerId));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(put("/api/players/{id}", playerId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)));
        });

        assertTrue(exception.getMessage().contains("Player not found"));
        verify(playerService).update(eq(playerId), any(PlayerEntity.class));
    }

    @Test
    void delete_shouldReturnNoContent_whenPlayerIsDeleted() throws Exception {
        // Arrange
        String playerId = UUID.randomUUID().toString();

        doNothing().when(playerService).delete(playerId);

        // Act & Assert
        mockMvc.perform(delete("/api/players/{id}", playerId))
                .andExpect(status().isNoContent());

        verify(playerService).delete(playerId);
    }

    @Test
    void delete_shouldThrowException_whenPlayerNotFound() throws Exception {
        // Arrange
        String playerId = UUID.randomUUID().toString();

        doThrow(new IllegalArgumentException("Player not found with id: " + playerId))
                .when(playerService).delete(playerId);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(delete("/api/players/{id}", playerId));
        });

        assertTrue(exception.getMessage().contains("Player not found"));
        verify(playerService).delete(playerId);
    }

    @Test
    void create_shouldHandleInvalidInput() throws Exception {
        // Arrange
        PlayerEntity invalidPlayer = PlayerEntity.builder()
                // Missing required fields
                .build();

        when(playerService.create(any(PlayerEntity.class)))
                .thenThrow(new IllegalArgumentException("Invalid player data"));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(post("/api/players")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidPlayer)));
        });

        assertTrue(exception.getMessage().contains("Invalid player data"));
    }

    @Test
    void update_shouldUpdateOnlyProvidedFields() throws Exception {
        // Arrange
        String playerId = UUID.randomUUID().toString();

        PlayerEntity partialUpdate = PlayerEntity.builder()
                .name("New Name Only")
                .build();

        PlayerEntity updatedPlayer = PlayerEntity.builder()
                .id(playerId)
                .name("New Name Only")
                .handedness(Handedness.RIGHT)
                .deleted(false)
                .build();

        when(playerService.update(eq(playerId), any(PlayerEntity.class))).thenReturn(updatedPlayer);

        // Act & Assert
        mockMvc.perform(put("/api/players/{id}", playerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partialUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(playerId)))
                .andExpect(jsonPath("$.name", is("New Name Only")))
                .andExpect(jsonPath("$.handedness", is("RIGHT")));

        verify(playerService).update(eq(playerId), any(PlayerEntity.class));
    }

    @Test
    void create_shouldHandleRightHandedPlayer() throws Exception {
        // Arrange
        String playerId = UUID.randomUUID().toString();
        PlayerEntity inputPlayer = PlayerEntity.builder()
                .name("Roger Federer")
                .handedness(Handedness.RIGHT)
                .build();

        PlayerEntity createdPlayer = PlayerEntity.builder()
                .id(playerId)
                .name("Roger Federer")
                .handedness(Handedness.RIGHT)
                .deleted(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(playerService.create(any(PlayerEntity.class))).thenReturn(createdPlayer);

        // Act & Assert
        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputPlayer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(playerId)))
                .andExpect(jsonPath("$.name", is("Roger Federer")))
                .andExpect(jsonPath("$.handedness", is("RIGHT")));

        verify(playerService).create(any(PlayerEntity.class));
    }

    @Test
    void update_shouldChangeHandedness() throws Exception {
        // Arrange
        String playerId = UUID.randomUUID().toString();

        PlayerEntity updateRequest = PlayerEntity.builder()
                .name("Player Name")
                .handedness(Handedness.LEFT)
                .build();

        PlayerEntity updatedPlayer = PlayerEntity.builder()
                .id(playerId)
                .name("Player Name")
                .handedness(Handedness.LEFT)
                .deleted(false)
                .build();

        when(playerService.update(eq(playerId), any(PlayerEntity.class))).thenReturn(updatedPlayer);

        // Act & Assert
        mockMvc.perform(put("/api/players/{id}", playerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.handedness", is("LEFT")));

        verify(playerService).update(eq(playerId), any(PlayerEntity.class));
    }
}