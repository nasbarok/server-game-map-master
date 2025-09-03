package com.airsoft.gamemapmaster.controller;

import com.airsoft.gamemapmaster.model.DTO.FieldDTO;
import com.airsoft.gamemapmaster.model.DTO.GameSessionDTO;
import com.airsoft.gamemapmaster.model.Field;
import com.airsoft.gamemapmaster.model.GameSession;
import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.repository.FieldRepository;
import com.airsoft.gamemapmaster.repository.GameSessionRepository;
import com.airsoft.gamemapmaster.service.GameSessionService;
import com.airsoft.gamemapmaster.service.HistoryService;
import com.airsoft.gamemapmaster.service.UserService;
import com.airsoft.gamemapmaster.service.impl.GameSessionServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

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

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private FieldRepository fieldRepository;

    /**
     * Récupère tous les terrains pour l'utilisateur authentifié
     */
    @GetMapping("/fields")
    public ResponseEntity<?> getFields(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Authentication authentication
    ) {
        try {
            // --- Authentification ---
            final String username = authentication.getName();
            var userOpt = userService.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
            }
            final Long ownerId = userOpt.get().getId();

            // --- Pagination & tri (openedAt desc, puis id desc ; nullsLast si supporté) ---
            int safeSize = Math.min(Math.max(size, 1), 100);
            Pageable pageable = PageRequest.of(
                    page,
                    safeSize,
                    Sort.by(
                            Sort.Order.desc("openedAt").nullsLast(), // Spring Data 3.x
                            Sort.Order.desc("id")
                    )
            );

            // --- Requête paginée en base ---
            // Option A (recommandée) : laisse le tri au Pageable
            Page<Field> fieldsPage = fieldRepository.findByOwnerIdOrderByOpenedAtDesc(ownerId, pageable);
            Page<FieldDTO> dtoPage = fieldsPage.map(FieldDTO::fromEntity);
            // Option B (si tu veux garder ta méthode spécifique) :
            // Page<Field> fieldsPage = fieldRepository.findByOwnerIdOrderByOpenedAtDesc(ownerId, pageable);

            // --- (Facultatif) mapping DTO pour ne pas exposer l’entité JPA ---
            // Page<FieldDTO> dtoPage = fieldsPage.map(FieldDTO::fromEntity);

            // --- Réponse standardisée ---
            Map<String, Object> response = new HashMap<>();
            response.put("content", dtoPage.getContent());
            response.put("totalElements", fieldsPage.getTotalElements());
            response.put("totalPages", fieldsPage.getTotalPages());
            response.put("number", fieldsPage.getNumber());
            response.put("size", fieldsPage.getSize());
            response.put("first", fieldsPage.isFirst());
            response.put("last", fieldsPage.isLast());
            response.put("numberOfElements", fieldsPage.getNumberOfElements());

            log.info("Terrains renvoyés (repo) pour ownerId {} : {} éléments (page {}/{})",
                    ownerId, fieldsPage.getNumberOfElements(), fieldsPage.getNumber(), fieldsPage.getTotalPages());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors du chargement des terrains : {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors du chargement des terrains: " + e.getMessage());
        }
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
     * Endpoint paginé pour sessions par terrain
     */
    @GetMapping("/fields/{fieldId}/sessions")
    public ResponseEntity<?> getFieldSessions(
            @PathVariable Long fieldId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            Authentication authentication
    ) {
        try {
            // --- Authentification & autorisation ---
            final String username = authentication.getName();
            var userOpt = userService.findByUsername(username);
            if (userOpt.isEmpty()) {
                log.warn("Utilisateur non trouvé pour le nom d'utilisateur : {}", username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            var fieldOpt = historyService.getFieldById(fieldId);
            if (fieldOpt.isEmpty()) {
                log.warn("Terrain non trouvé pour l'ID : {}", fieldId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            Field field = fieldOpt.get();

            // Règle d’accès à adapter (ex: propriétaire ou rôle HOST)
/*
            if (!field.getOwner().getId().equals(userOpt.get().getId())) {
                log.warn("Accès refusé: user {} tente d'accéder au terrain {}", username, fieldId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
*/

            // --- Pagination & tri ---
            int safeSize = Math.min(Math.max(size, 1), 100); // borne 1..100
            Pageable pageable = PageRequest.of(page, safeSize, Sort.by("startTime").descending());

            // --- Validation des bornes temporelles ---
            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().body("startDate doit être ≤ endDate");
            }

            // --- Requête paginée avec filtres OffsetDateTime (null-safe) ---
            Page<GameSession> sessionsPage = gameSessionRepository
                    .findByFieldIdWithDateFilter(fieldId, startDate, endDate, pageable);

            // --- Mapping DTO ---
            Page<GameSessionDTO> dtoPage = sessionsPage.map(GameSessionDTO::fromEntity);

            // --- Réponse standardisée (métadonnées de pagination) ---
            Map<String, Object> response = new HashMap<>();
            response.put("content", dtoPage.getContent());
            response.put("totalElements", dtoPage.getTotalElements());
            response.put("totalPages", dtoPage.getTotalPages());
            response.put("number", dtoPage.getNumber());
            response.put("size", dtoPage.getSize());
            response.put("first", dtoPage.isFirst());
            response.put("last", dtoPage.isLast());
            response.put("numberOfElements", dtoPage.getNumberOfElements());

            log.info("Sessions renvoyées pour fieldId {} : {} éléments (page {}/{})",
                    fieldId, dtoPage.getNumberOfElements(), dtoPage.getNumber(), dtoPage.getTotalPages());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors du chargement des sessions pour fieldId {} : {}", fieldId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors du chargement des sessions: " + e.getMessage());
        }
    }


    /**
     * Récupère toutes les sessions de jeu auxquelles l'utilisateur authentifié a participé
     */
    @GetMapping("/sessions")
    public ResponseEntity<?> getAllSessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            Authentication authentication
    ) {
        try {
            // --- Authentification ---
            final String username = authentication.getName();
            var userOpt = userService.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
            }
            final Long userId = userOpt.get().getId();

            // --- Pagination & tri ---
            int safeSize = Math.min(Math.max(size, 1), 100); // borne 1..100
            Pageable pageable = PageRequest.of(page, safeSize, Sort.by("startTime").descending());

            // --- Validation des bornes temporelles ---
            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().body("startDate doit être ≤ endDate");
            }

            // --- Requête paginée (null-safe sur les dates) ---
            Page<GameSession> sessionsPage = gameSessionRepository
                    .findByUserIdWithDateFilter(userId, startDate, endDate, pageable);

            // --- Mapping DTO ---
            Page<GameSessionDTO> dtoPage = sessionsPage.map(GameSessionDTO::fromEntity);

            // --- Réponse standardisée (type Page) ---
            Map<String, Object> response = new HashMap<>();
            response.put("content", dtoPage.getContent());
            response.put("totalElements", dtoPage.getTotalElements());
            response.put("totalPages", dtoPage.getTotalPages());
            response.put("number", dtoPage.getNumber());
            response.put("size", dtoPage.getSize());
            response.put("first", dtoPage.isFirst());
            response.put("last", dtoPage.isLast());
            response.put("numberOfElements", dtoPage.getNumberOfElements());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors du chargement des sessions pour l'utilisateur courant : {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors du chargement des sessions: " + e.getMessage());
        }
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

        List<GameSession> sessions = gameSessionRepository.findByFieldId(id);
        for (GameSession session : sessions) {
            historyService.deleteGameSession(session.getId());
        }

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
