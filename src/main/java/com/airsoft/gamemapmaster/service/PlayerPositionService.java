package com.airsoft.gamemapmaster.service;
import com.airsoft.gamemapmaster.model.DTO.GameSessionPositionHistoryDTO;
import com.airsoft.gamemapmaster.model.DTO.PlayerPositionDTO;

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
     *
     * @param gameSessionId Identifiant de la session de jeu
     * @return DTO contenant l'historique des positions
     */
    GameSessionPositionHistoryDTO getPositionHistory(Long gameSessionId);
}
