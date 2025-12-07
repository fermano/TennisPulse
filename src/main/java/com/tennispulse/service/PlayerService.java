package com.tennispulse.service;

import com.tennispulse.domain.PlayerEntity;
import com.tennispulse.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerEntity create(PlayerEntity player) {
        player.setId(null);
        PlayerEntity created = playerRepository.save(player);
        log.info("Player created: id={}, name={}", created.getId(), created.getName());
        return created;
    }

    public List<PlayerEntity> findAll() {
        return playerRepository.findByDeletedFalse();
    }

    public PlayerEntity findById(UUID id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + id));
    }

    public PlayerEntity update(UUID id, PlayerEntity updated) {
        PlayerEntity existing = findById(id);
        existing.setName(updated.getName());
        existing.setHandedness(updated.getHandedness());

        PlayerEntity saved = playerRepository.save(existing);
        log.info("Player updated: id={}, name={}, handedness={}",
                saved.getId(), saved.getName(), saved.getHandedness());
        return saved;
    }

    // Soft delete
    public void delete(UUID id) {
        PlayerEntity existing = findById(id);

        if (existing.isDeleted()) {
            log.info("Player already soft-deleted: id={}", id);
            return;
        }

        existing.setDeleted(true);
        existing.setDeletedAt(Instant.now());
        existing.setName("Deleted player " + id.toString().substring(0, 8));
        existing.setHandedness(null);

        playerRepository.save(existing);
        log.info("Player soft-deleted: id={}", id);
    }
}