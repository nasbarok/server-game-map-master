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
@RequestMapping("/api/game")
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
    @PostMapping("/{fieldId}/start")
    public ResponseEntity<?> startGameFromField(
            @PathVariable Long fieldId,
            Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }

        Optional<Field> fieldOpt = fieldService.findById(fieldId);
        if (fieldOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Terrain non trouvé");
        }

        Field field = fieldOpt.get();

        // Vérifier que le terrain appartient bien à l'utilisateur
        if (!field.getOwner().getId().equals(userOpt.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Non autorisé à démarrer une partie sur ce terrain");
        }
        // 👉 Ici, tu peux démarrer ta vraie logique GameSession : création en base
        GameSession session = gameSessionService.startNewSession(field);

        return ResponseEntity.ok(Map.of(
                "message", "Partie démarrée avec succès",
                "gameSessionId", session.getId(),
                "fieldId", field.getId()
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



    @GetMapping("/current-session/{fieldId}")
    public ResponseEntity<?> getGameSessionByFieldId(@PathVariable Long fieldId) {
        Optional<GameSession> sessionOpt = gameSessionService.findActiveSessionByFieldId(fieldId);

        if (sessionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session de jeu non trouvée");
        }

        GameSession session = sessionOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("gameSessionId", session.getId());
        response.put("fieldId", session.getField().getId());
        response.put("fieldName", session.getField().getName());
        response.put("status", session.getStatus());
        response.put("active", session.isActive());
        response.put("startTime", session.getStartTime());
        response.put("endTime", session.getEndTime());

        return ResponseEntity.ok(response);
    }
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<?> getGameSessionById(@PathVariable Long sessionId) {
        Optional<GameSession> sessionOpt = gameSessionService.findById(sessionId);

        if (sessionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session de jeu non trouvée");
        }

        GameSession session = sessionOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("gameSessionId", session.getId());
        response.put("fieldId", session.getField().getId());
        response.put("fieldName", session.getField().getName());
        response.put("status", session.getStatus());
        response.put("active", session.isActive());
        response.put("startTime", session.getStartTime());
        response.put("endTime", session.getEndTime());

        return ResponseEntity.ok(response);
    }
}
