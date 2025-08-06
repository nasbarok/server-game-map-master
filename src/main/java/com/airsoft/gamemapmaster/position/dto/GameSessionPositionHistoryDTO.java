package com.airsoft.gamemapmaster.position.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO représentant l'historique des positions des joueurs pour une session de jeu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameSessionPositionHistoryDTO {
    
    private Long gameSessionId;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private Map<Long, List<PlayerPositionDTO>> playerPositions;
}
