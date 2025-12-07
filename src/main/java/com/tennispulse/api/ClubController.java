package com.tennispulse.api;

import com.tennispulse.domain.ClubEntity;
import com.tennispulse.service.ClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;

    @PostMapping
    public ResponseEntity<ClubEntity> create(@RequestBody ClubEntity club) {
        ClubEntity created = clubService.create(club);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<ClubEntity> list() {
        return clubService.findAll();
    }

    @GetMapping("/{id}")
    public ClubEntity get(@PathVariable UUID id) {
        return clubService.findById(id);
    }

    @PutMapping("/{id}")
    public ClubEntity update(@PathVariable UUID id, @RequestBody ClubEntity club) {
        return clubService.update(id, club);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        clubService.delete(id);
    }
}
