package com.tennispulse.repository;

import com.tennispulse.domain.ClubEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface ClubRepository extends JpaRepository<ClubEntity, UUID> {
    List<ClubEntity> findByDeletedFalse();
}
