package com.airsoft.gamemapmaster.controller;

import com.airsoft.gamemapmaster.model.*;
import com.airsoft.gamemapmaster.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/games")
public class GameSessionController {
    private static final Logger logger = LoggerFactory.getLogger(GameSessionController.class);
    @Autowired
    private GameMapService gameMapService;
    
    @Autowired
    private ScenarioService scenarioService;
    
    @Autowired
    private UserService userService;

    @Autowired
    private FieldService fieldService;

    @Autowired
    private GameSessionService gameSessionService;
    
    /**
     * Démarre une partie sur une carte
     */
    @PostMapping("/maps/{mapId}/start")
    public ResponseEntity<?> startGame(@PathVariable("mapId") Long mapId,
                                      @RequestParam("scenarioId") Long scenarioId,
                                      Authentication authentication) {
        String username = authentication.getName();
        Optional<User> user = userService.findByUsername(username);
        
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }
        
        // Vérifier si l'utilisateur est le propriétaire de la carte
        Optional<GameMap> gameMapOpt = gameMapService.findById(mapId);
        if (gameMapOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Carte non trouvée");
        }
        
        GameMap gameMap = gameMapOpt.get();
        if (!gameMap.getOwner().getId().equals(user.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Vous n'êtes pas autorisé à démarrer une partie sur cette carte");
        }
        
        // Vérifier si le scénario existe et est associé à cette carte
        Optional<Scenario> scenarioOpt = scenarioService.findById(scenarioId);
        if (scenarioOpt.isEmpty() || !scenarioOpt.get().getGameMap().getId().equals(mapId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Scénario non trouvé ou non associé à cette carte");
        }
        
        // Logique pour démarrer la partie
        // Dans une implémentation réelle, on pourrait mettre à jour le statut du scénario, 
        // envoyer des notifications aux joueurs connectés, etc.
        
        return ResponseEntity.ok(Map.of(
                "message", "Partie démarrée avec succès",
                "gameMap", gameMap,
                "scenario", scenarioOpt.get()
        ));
    }
    
    /**
     * Termine une partie sur une carte
     */
    @PostMapping("/maps/{mapId}/end")
    public ResponseEntity<?> endGame(@PathVariable("mapId") Long mapId,
                                    Authentication authentication) {
        String username = authentication.getName();
        Optional<User> user = userService.findByUsername(username);
        
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }
        
        // Vérifier si l'utilisateur est le propriétaire de la carte
        Optional<GameMap> gameMapOpt = gameMapService.findById(mapId);
        if (gameMapOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Carte non trouvée");
        }
        
        GameMap gameMap = gameMapOpt.get();
        if (!gameMap.getOwner().getId().equals(user.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Vous n'êtes pas autorisé à terminer une partie sur cette carte");
        }
        
        // Logique pour terminer la partie
        // Dans une implémentation réelle, on pourrait mettre à jour le statut du scénario,
        // calculer les scores, envoyer des notifications aux joueurs, etc.
        
        return ResponseEntity.ok(Map.of(
                "message", "Partie terminée avec succès",
                "gameMap", gameMap
        ));
    }

    @GetMapping("/{fieldId}/status")
    public ResponseEntity<?> getFieldStatus(@PathVariable("fieldId") Long fieldId) {
        Optional<Field> fieldOpt = fieldService.findById(fieldId);
        if (fieldOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Terrain non trouvé");
        }

        Field field = fieldOpt.get();

        // Récupérer la session de jeu active pour ce terrain
        Optional<GameSession> activeSession = gameSessionService.findActiveSessionByFieldId(fieldId);

        Map<String, Object> response = new HashMap<>();
        response.put("fieldId", fieldId);
        response.put("fieldName", field.getName());

        if (activeSession.isPresent()) {
            GameSession session = activeSession.get();
            response.put("status", session.getStatus());
            response.put("active", session.isActive());
            response.put("startTime", session.getStartTime());
            response.put("endTime", session.getEndTime());
        } else {
            response.put("status", "INACTIVE");
            response.put("active", false);
        }

        return ResponseEntity.ok(response);
    }
}
