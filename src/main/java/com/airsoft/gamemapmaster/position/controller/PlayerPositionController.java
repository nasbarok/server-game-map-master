package com.airsoft.gamemapmaster.position.controller;

import com.airsoft.gamemapmaster.controller.GameSessionController;
import com.airsoft.gamemapmaster.position.dto.GameSessionPositionHistoryDTO;
import com.airsoft.gamemapmaster.position.dto.PlayerPositionDTO;
import com.airsoft.gamemapmaster.position.service.PlayerPositionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller REST pour la gestion des positions des joueurs
 */
@RestController
@RequestMapping("/api")
public class PlayerPositionController {
    private static final Logger logger = LoggerFactory.getLogger(GameSessionController.class);

    @Autowired
    private PlayerPositionService playerPositionService;

    /**
     * Récupère l'historique des positions pour une session de jeu
     * @param gameSessionId ID de la session de jeu
     * @return L'historique des positions
     */
    @GetMapping("/game-sessions/{gameSessionId}/position-history")
    public ResponseEntity<GameSessionPositionHistoryDTO> getPositionHistory(@PathVariable Long gameSessionId) {
        GameSessionPositionHistoryDTO history = playerPositionService.getPositionHistory(gameSessionId);
        return ResponseEntity.ok(history);
    }

    /**
     * Récupère l'historique des positions pour un joueur dans une session de jeu
     * @param gameSessionId ID de la session de jeu
     * @param userId ID de l'utilisateur
     * @return L'historique des positions du joueur
     */
    @GetMapping("/game-sessions/{gameSessionId}/players/{userId}/position-history")
    public ResponseEntity<GameSessionPositionHistoryDTO> getPlayerPositionHistory(
            @PathVariable Long gameSessionId,
            @PathVariable Long userId) {
        GameSessionPositionHistoryDTO history = playerPositionService.getPlayerPositionHistory(gameSessionId, userId);
        return ResponseEntity.ok(history);
    }

    /**
     * Récupère l'historique des positions pour une équipe dans une session de jeu
     * @param gameSessionId ID de la session de jeu
     * @param teamId ID de l'équipe
     * @return L'historique des positions de l'équipe
     */
    @GetMapping("/game-sessions/{gameSessionId}/teams/{teamId}/position-history")
    public ResponseEntity<GameSessionPositionHistoryDTO> getTeamPositionHistory(
            @PathVariable Long gameSessionId,
            @PathVariable Long teamId) {
        GameSessionPositionHistoryDTO history = playerPositionService.getTeamPositionHistory(gameSessionId, teamId);
        return ResponseEntity.ok(history);
    }

    /**
     * Enregistre manuellement une position (pour les tests ou les cas spéciaux)
     * @param positionDTO DTO contenant les informations de position
     * @return La position enregistrée
     */
    @PostMapping("/positions")
    public ResponseEntity<PlayerPositionDTO> savePosition(@RequestBody PlayerPositionDTO positionDTO) {
        PlayerPositionDTO savedPosition = playerPositionService.savePosition(positionDTO);
        return ResponseEntity.ok(savedPosition);
    }

    /**
     * Retourne la dernière position connue de chaque joueur pour un terrain donné.
     */
    @GetMapping("/field/{fieldId}/positions")
    public ResponseEntity<Map<Long, PlayerPositionDTO>> getLastKnownPositions(@PathVariable Integer fieldId) {
        logger.info("📥 [getLastKnownPositions] Requête pour les positions connues du terrain {}", fieldId);
        Map<Long, PlayerPositionDTO> positions = playerPositionService.getLastKnownPositionsByField(fieldId);
        return ResponseEntity.ok(positions);
    }
}
