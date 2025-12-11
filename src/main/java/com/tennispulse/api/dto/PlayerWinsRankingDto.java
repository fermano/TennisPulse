package com.tennispulse.api;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class PlayerWinsRankingDto {
    private UUID playerId;
    private String playerName;
    private long wins;
}
