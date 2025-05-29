package com.airsoft.gamemapmaster.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Map<Long, List<PlayerPositionDTO>> playerPositions;
}
