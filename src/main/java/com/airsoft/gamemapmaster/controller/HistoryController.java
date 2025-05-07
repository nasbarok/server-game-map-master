package com.airsoft.gamemapmaster.controller;

import com.airsoft.gamemapmaster.model.DTO.GameSessionDTO;
import com.airsoft.gamemapmaster.model.Field;
import com.airsoft.gamemapmaster.model.GameSession;
import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.service.HistoryService;
import com.airsoft.gamemapmaster.service.UserService;
import com.airsoft.gamemapmaster.service.impl.GameSessionServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Contrôleur pour gérer les endpoints liés à l'historique des parties
 */
@RestController
@RequestMapping("/api/history")
public class HistoryController {
    private static final Logger log = LoggerFactory.getLogger(HistoryController.class);

    @Autowired
    private HistoryService historyService;

    @Autowired
    private UserService userService;

    /**
     * Récupère tous les terrains pour l'utilisateur authentifié
     */
    @GetMapping("/fields")
    public ResponseEntity<?> getFields(Authentication authentication) {
        log.info("Récupération des terrains pour l'utilisateur authentifié");
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }

        List<Field> fields = historyService.getFieldsByOwnerId(userOpt.get().getId());
        log.info("Nombre de terrains trouvés : {}", fields.size());
        return ResponseEntity.ok(fields);
    }

    /**
     * Récupère un terrain par son ID
     */
    @GetMapping("/fields/{id}")
    public ResponseEntity<?> getFieldById(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }

        Optional<Field> fieldOpt = historyService.getFieldById(id);
        if (fieldOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Terrain non trouvé");
        }

        Field field = fieldOpt.get();
      /*  if (!field.getOwner().getId().equals(userOpt.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Vous n'êtes pas autorisé à accéder à ce terrain");
        }*/

        return ResponseEntity.ok(field);
    }

    /**
     * Récupère toutes les sessions de jeu pour un terrain donné
     */
    @GetMapping("/fields/{fieldId}/sessions")
    public ResponseEntity<List<GameSessionDTO>> getGameSessionsByFieldId(@PathVariable Long fieldId, Authentication authentication) {
        log.info("Récupération des sessions de jeu pour le terrain avec ID : {}", fieldId);
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);

        if (userOpt.isEmpty()) {
            log.warn("Utilisateur non trouvé pour le nom d'utilisateur : {}", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Field> fieldOpt = historyService.getFieldById(fieldId);
        if (fieldOpt.isEmpty()) {
            log.warn("Terrain non trouvé pour l'ID : {}", fieldId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Field field = fieldOpt.get();
/*        if (!field.getOwner().getId().equals(userOpt.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }*/

        List<GameSession> gameSessions = historyService.getGameSessionsByFieldId(fieldId);
        List<GameSessionDTO> gameSessionDTOs = new ArrayList<>();
        for (GameSession gameSession : gameSessions) {
            GameSessionDTO gameSessionDTO = GameSessionDTO.fromEntity(gameSession);
            gameSessionDTOs.add(gameSessionDTO);
        }
        log.info("Nombre de sessions de jeu trouvées : {}", gameSessions.size());
        return ResponseEntity.ok(gameSessionDTOs);
    }

    /**
     * Récupère toutes les sessions de jeu auxquelles l'utilisateur authentifié a participé
     */
    @GetMapping("/sessions")
    public ResponseEntity<?> getGameSessions(Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }

        List<GameSession> gameSessions = historyService.getGameSessionsByParticipantId(userOpt.get().getId());
        return ResponseEntity.ok(gameSessions);
    }

    /**
     * Récupère une session de jeu par son ID
     */
    @GetMapping("/sessions/{id}")
    public ResponseEntity<GameSessionDTO> getGameSessionById(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<GameSession> gameSessionOpt = historyService.getGameSessionById(id);
        if (gameSessionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        GameSession gameSession = gameSessionOpt.get();
        if (!historyService.isUserAuthorizedForGameSession(userOpt.get(), gameSession)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        GameSessionDTO gameSessionDTO = GameSessionDTO.fromEntity(gameSession);

        return ResponseEntity.ok(gameSessionDTO);
    }

    /**
     * Supprime une session de jeu
     */
    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<?> deleteGameSession(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }

        Optional<GameSession> gameSessionOpt = historyService.getGameSessionById(id);
        if (gameSessionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session de jeu non trouvée");
        }

        GameSession gameSession = gameSessionOpt.get();
        if (!gameSession.getField().getOwner().getId().equals(userOpt.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Vous n'êtes pas autorisé à supprimer cette session de jeu");
        }

        historyService.deleteGameSession(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Récupère les statistiques d'une session de jeu
     */
    @GetMapping("/sessions/{id}/statistics")
    public ResponseEntity<?> getGameSessionStatistics(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }

        Optional<GameSession> gameSessionOpt = historyService.getGameSessionById(id);
        if (gameSessionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session de jeu non trouvée");
        }

        GameSession gameSession = gameSessionOpt.get();
        if (!historyService.isUserAuthorizedForGameSession(userOpt.get(), gameSession)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Vous n'êtes pas autorisé à accéder à cette session de jeu");
        }

        Map<String, Object> statistics = historyService.getGameSessionStatistics(id);
        return ResponseEntity.ok(statistics);
    }
    /**
     * Supprime un terrain et tout son historique (sessions liées)
     */
    @DeleteMapping("/fields/{id}")
    public ResponseEntity<?> deleteField(@PathVariable Long id, Authentication authentication) {
        log.info("Suppression du terrain avec ID : {}", id);
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }

        Optional<Field> fieldOpt = historyService.getFieldById(id);
        if (fieldOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Terrain non trouvé");
        }

        Field field = fieldOpt.get();
        if (!field.getOwner().getId().equals(userOpt.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Vous n'êtes pas autorisé à supprimer ce terrain");
        }

        historyService.deleteFieldAndHistory(id);
        log.info("Terrain avec ID : {} supprimé avec succès", id);
        return ResponseEntity.noContent().build();
    }


}
