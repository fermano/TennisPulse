package com.tennispulse.service;

import com.tennispulse.domain.ClubEntity;
import com.tennispulse.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;

    public ClubEntity create(ClubEntity club) {
        club.setId(null);
        ClubEntity createdClub = clubRepository.save(club);
        log.info("Club created: id={}, name={}", createdClub.getId(), createdClub.getName());
        return createdClub;
    }

    public List<ClubEntity> findAll() {
        return clubRepository.findByDeletedFalse();
    }

    public ClubEntity findById(String id) {
        return clubRepository.findById(id)                .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Club not found: " + id
        ));
    }

    public ClubEntity update(String id, ClubEntity updated) {
        ClubEntity existing = findById(id);
        existing.setName(updated.getName());
        existing.setCity(updated.getCity());
        existing.setCountry(updated.getCountry());

        ClubEntity saved = clubRepository.save(existing);
        log.info("Club updated: id={}, name={}, city={}, country={}",
                saved.getId(), saved.getName(), saved.getCity(), saved.getCountry());
        return saved;
    }

    public void delete(String id) {
        ClubEntity existing = findById(id);

        if (existing.isDeleted()) {
            log.info("Club already soft-deleted: id={}", id);
            return;
        }

        existing.setDeleted(true);
        existing.setDeletedAt(Instant.now());
        existing.setName("Deleted club " + id.substring(0, 8));
        existing.setCity(null);
        existing.setCountry(null);

        clubRepository.save(existing);
        log.info("Club soft-deleted: id={}", id);
    }
}
