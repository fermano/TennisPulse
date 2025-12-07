package com.tennispulse.service;

import com.tennispulse.domain.ClubEntity;
import com.tennispulse.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;

    public ClubEntity create(ClubEntity club) {
        club.setId(null);
        return clubRepository.save(club);
    }

    public List<ClubEntity> findAll() {
        return clubRepository.findByDeletedFalse();
    }

    public ClubEntity findById(UUID id) {
        return clubRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Club not found: " + id));
    }

    public ClubEntity update(UUID id, ClubEntity updated) {
        ClubEntity existing = findById(id);
        existing.setName(updated.getName());
        existing.setCity(updated.getCity());
        existing.setCountry(updated.getCountry());
        return clubRepository.save(existing);
    }

    public void delete(UUID id) {
        ClubEntity existing = findById(id);

        if (existing.isDeleted()) {
            return;
        }

        existing.setDeleted(true);
        existing.setDeletedAt(Instant.now());
        existing.setName("Deleted club " + id.toString().substring(0, 8));
        existing.setCity(null);
        existing.setCountry(null);

        clubRepository.save(existing);
    }
}