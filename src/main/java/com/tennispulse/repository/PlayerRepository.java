package com.tennispulse.repository;

import com.tennispulse.domain.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;

public interface PlayerRepository extends JpaRepository<PlayerEntity, UUID> {
    List<PlayerEntity> findByDeletedFalse();
}
