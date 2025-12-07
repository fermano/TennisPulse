package com.tennispulse.repository;

import com.tennispulse.domain.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlayerRepository extends JpaRepository<PlayerEntity, UUID> {
}
