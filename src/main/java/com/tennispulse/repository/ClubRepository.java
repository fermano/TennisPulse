package com.tennispulse.repository;

import com.tennispulse.domain.ClubEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ClubRepository extends JpaRepository<ClubEntity, UUID> {
}
