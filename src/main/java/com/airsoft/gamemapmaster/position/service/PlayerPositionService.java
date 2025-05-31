package com.airsoft.gamemapmaster.position.service;
import com.airsoft.gamemapmaster.position.dto.GameSessionPositionHistoryDTO;
import com.airsoft.gamemapmaster.position.dto.PlayerPositionDTO;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public interface PlayerPositionService {

    /**
     * Sauvegarde une position de joueur
     *
     * @param positionDTO DTO contenant les informations de position
     * @return DTO de la position sauvegardée
     */
    PlayerPositionDTO savePosition(PlayerPositionDTO positionDTO);

    /**
     * Récupère l'historique des positions pour une session de jeu
     * @param gameSessionId ID de la session de jeu
     * @return L'historique des positions structuré par joueur
     */
    GameSessionPositionHistoryDTO getPositionHistory(Long gameSessionId);

    /**
     * Récupère l'historique des positions pour un joueur dans une session de jeu
     * @param gameSessionId ID de la session de jeu
     * @param userId ID de l'utilisateur
     * @return L'historique des positions du joueur
     */
    GameSessionPositionHistoryDTO getPlayerPositionHistory(Long gameSessionId, Long userId);

    /**
     * Récupère l'historique des positions pour une équipe dans une session de jeu
     * @param gameSessionId ID de la session de jeu
     * @param teamId ID de l'équipe
     * @return L'historique des positions de l'équipe
     */
    GameSessionPositionHistoryDTO getTeamPositionHistory(Long gameSessionId, Long teamId);

    Map<Long, PlayerPositionDTO> getLastKnownPositionsByField(Integer fieldId);
    Optional<Instant> getLastSavedTimestamp(Long userId);
}
