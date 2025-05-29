package com.airsoft.gamemapmaster.controller;

import com.airsoft.gamemapmaster.model.DTO.GameSessionPositionHistoryDTO;
import com.airsoft.gamemapmaster.model.DTO.PlayerPositionDTO;
import com.airsoft.gamemapmaster.service.PlayerPositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST pour la gestion des positions des joueurs
 */
@RestController
@RequestMapping("/api/game-sessions/{gameSessionId}/positions")
public class PlayerPositionController {
    
    @Autowired
    private PlayerPositionService playerPositionService;
    
    /**
     * Récupère l'historique des positions pour une session de jeu
     * 
     * @param gameSessionId Identifiant de la session de jeu
     * @return Historique des positions
     */
    @GetMapping
    public ResponseEntity<GameSessionPositionHistoryDTO> getPositionHistory(@PathVariable Long gameSessionId) {
        GameSessionPositionHistoryDTO history = playerPositionService.getPositionHistory(gameSessionId);
        return ResponseEntity.ok(history);
    }
    
    /**
     * Sauvegarde une position de joueur
     * 
     * @param gameSessionId Identifiant de la session de jeu
     * @param positionDTO DTO contenant les informations de position
     * @return Position sauvegardée
     */
    @PostMapping
    public ResponseEntity<PlayerPositionDTO> savePosition(
            @PathVariable Long gameSessionId,
            @RequestBody PlayerPositionDTO positionDTO) {
        
        // Vérifier que le gameSessionId correspond
        if (!gameSessionId.equals(positionDTO.getGameSessionId())) {
            return ResponseEntity.badRequest().build();
        }
        
        PlayerPositionDTO savedPosition = playerPositionService.savePosition(positionDTO);
        return ResponseEntity.ok(savedPosition);
    }
}
