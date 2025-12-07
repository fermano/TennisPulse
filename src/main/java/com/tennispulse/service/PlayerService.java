package com.tennispulse.service;

import com.tennispulse.domain.PlayerEntity;
import com.tennispulse.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerEntity create(PlayerEntity player) {
        player.setId(null); // ensure new
        return playerRepository.save(player);
    }

    public List<PlayerEntity> findAll() {
        return playerRepository.findAll();
    }

    public PlayerEntity findById(UUID id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + id));
    }

    public PlayerEntity update(UUID id, PlayerEntity updated) {
        PlayerEntity existing = findById(id);
        existing.setName(updated.getName());
        existing.setHandedness(updated.getHandedness());
        return playerRepository.save(existing);
    }

    public void delete(UUID id) {
        playerRepository.deleteById(id);
    }
}
