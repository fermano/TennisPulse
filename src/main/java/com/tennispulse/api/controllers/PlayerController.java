package com.tennispulse.api.controllers;

import com.tennispulse.domain.PlayerEntity;
import com.tennispulse.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @PostMapping
    public ResponseEntity<PlayerEntity> create(@RequestBody PlayerEntity player) {
        PlayerEntity created = playerService.create(player);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<PlayerEntity> list() {
        return playerService.findAll();
    }

    @GetMapping("/{id}")
    public PlayerEntity get(@PathVariable String id) {
        return playerService.findById(id);
    }

    @PutMapping("/{id}")
    public PlayerEntity update(@PathVariable String id, @RequestBody PlayerEntity player) {
        return playerService.update(id, player);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        playerService.delete(id);
    }
}
