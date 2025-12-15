package com.tennispulse.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class PlayerWinsRankingDto {
    private String playerId;
    private String playerName;
    private long wins;
}
