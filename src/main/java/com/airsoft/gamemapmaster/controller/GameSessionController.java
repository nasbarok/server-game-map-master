package com.airsoft.gamemapmaster.controller;

import com.airsoft.gamemapmaster.model.*;
import com.airsoft.gamemapmaster.model.DTO.GameSessionDTO;
import com.airsoft.gamemapmaster.model.DTO.GameSessionParticipantDTO;
import com.airsoft.gamemapmaster.model.DTO.GameSessionScenarioDTO;
import com.airsoft.gamemapmaster.service.*;
import com.airsoft.gamemapmaster.websocket.WebSocketMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@RestController
@RequestMapping("/api/game-sessions")
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

    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    @PostMapping
    public ResponseEntity<GameSessionDTO> createGameSession(@RequestBody GameSession gameSession) {
        logger.info("📥 Requête POST /api/game-sessions reçue pour création de session");
        logger.debug("📦 Données reçues : FieldID={}, GameMapID={}, durée={} min",
                gameSession.getField() != null ? gameSession.getField().getId() : "null",
                gameSession.getGameMap() != null ? gameSession.getGameMap().getId() : "null",
                gameSession.getDurationMinutes());

        GameSession createdSession = gameSessionService.createGameSession(gameSession);
        logger.info("✅ Session créée : ID={}, active={}, start={}",
                createdSession.getId(), createdSession.getActive(), createdSession.getStartTime());

        GameSessionDTO gameSessionDTO = GameSessionDTO.fromEntity(createdSession);
        return ResponseEntity.status(HttpStatus.CREATED).body(gameSessionDTO);
    }


    @PostMapping("/{id}/start")
    public ResponseEntity<GameSessionDTO> startGameSession(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        logger.info("▶️ Requête POST /api/game-sessions/{}/start reçue", id);

        String startTimeStr = payload.get("startTime");
        LocalDateTime startTime;
        try {
            Instant instant = Instant.parse(startTimeStr);
            startTime = LocalDateTime.ofInstant(instant, ZoneId.of("UTC"));
        } catch (Exception e) {
            logger.error("Failed to parse startTime", e);
            logger.error("Payload: {}", payload);
            logger.error("startTimeStr: {}", startTimeStr);
            return ResponseEntity.badRequest().body(null); // ou un DTO d'erreur
        }

        GameSession startedSession = gameSessionService.startGameSession(id, startTime);
        logger.info("🎮 Session démarrée : ID={}, start={}, nbParticipants={}, nbScénarios={}",
                startedSession.getId(),
                startedSession.getStartTime(),
                startedSession.getParticipants().size(),
                startedSession.getScenarios().size()
        );

        // Notifier via WebSocket
        WebSocketMessage message = WebSocketMessage.gameSessionStarted(
                startedSession,
                startedSession.getField().getOwner().getId()
        );
        messagingTemplate.convertAndSend("/topic/field/" + startedSession.getField().getId(), message);
        logger.info("📡 Message WebSocket GAME_SESSION_STARTED envoyé sur /topic/field/{}", startedSession.getField().getId());

        GameSessionDTO dto = GameSessionDTO.fromEntity(startedSession);
        logger.debug("📤 DTO renvoyé : ID={}, Field={}, GameMap={}, nbParticipants={}, nbScénarios={}",
                dto.getId(),
                dto.getField() != null ? dto.getField().getId() : "null",
                dto.getGameMap() != null ? dto.getGameMap().getId() : "null",
                dto.getParticipants().size(),
                dto.getScenarios().size()
        );

        return ResponseEntity.ok(dto);
    }

    /**
     * Termine une partie sur une carte
     */
    @PostMapping("/{id}/end")
    public ResponseEntity<GameSessionDTO> endGameSession(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {

        logger.info("▶️ Requête POST /api/game-sessions/{}/end reçue", id);

        String endTimeStr = payload.get("endTime");
        LocalDateTime endTime;
        try {
            Instant instant = Instant.parse(endTimeStr);
            endTime = LocalDateTime.ofInstant(instant, ZoneId.of("UTC"));
        } catch (Exception e) {
            logger.error("Failed to parse endTime", e);
            logger.error("Payload: {}", payload);
            logger.error("endTimeStr: {}", endTimeStr);
            return ResponseEntity.badRequest().body(null); // ou un DTO d'erreur
        }

        GameSession endedSession = gameSessionService.endGameSession(id, endTime);


        // Notifier tous les participants via WebSocket
        WebSocketMessage message = WebSocketMessage.gameSessionEnded(
                endedSession,
                endedSession.getField().getOwner().getId()
        );

        messagingTemplate.convertAndSend("/topic/field/" + endedSession.getField().getId(), message);

        logger.info("📡 Message WebSocket GAME_SESSION_ENDED envoyé sur /topic/field/{}", endedSession.getField().getId());

        GameSessionDTO gameSessionDTO = GameSessionDTO.fromEntity(endedSession);
        return ResponseEntity.ok(gameSessionDTO);
    }


    /**
     * Démarre une partie sur une carte
     */
    @PostMapping("field/{fieldId}/start")
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

    @GetMapping("/{id}")
    public ResponseEntity<GameSessionDTO> getGameSession(@PathVariable Long id) {
        logger.info("🔍 Requête GET /api/game-sessions/{} reçue", id);
        Optional<GameSession> sessionOpt = gameSessionService.findById(id);
        if (sessionOpt.isEmpty()) {
            logger.warn("⚠️ Session de jeu introuvable pour l'ID={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        GameSession session = sessionOpt.get();
        logger.info("✅ Session trouvée : ID={}, participants={}, scénarios={}",
                session.getId(),
                session.getParticipants().size(),
                session.getScenarios().size()
        );

        try {
            GameSessionDTO gameSessionDTO = GameSessionDTO.fromEntity(session);
            logger.debug("🛠️ DTO créé avec succès pour la session ID={}", id);
            return ResponseEntity.ok(gameSessionDTO);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la conversion GameSession en DTO pour ID={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/active")
    public ResponseEntity<List<GameSessionDTO>> getAllActiveGameSessions() {
        List<GameSession> activeSessions = gameSessionService.findAllActiveGameSessions();
        if (activeSessions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        // Convertir les sessions en DTOs
        List<GameSessionDTO> gameSessionDTOs = new ArrayList<>();
        for (GameSession session : activeSessions) {
            GameSessionDTO gameSessionDTO = GameSessionDTO.fromEntity(session);
            gameSessionDTOs.add(gameSessionDTO);
        }

        return ResponseEntity.ok(gameSessionDTOs);
    }

    @GetMapping("/map/{gameMapId}")
    public ResponseEntity<List<GameSessionDTO>> getGameSessionsByGameMap(@PathVariable Long gameMapId) {
        Optional<GameMap> gameMapOpt = gameMapService.findById(gameMapId);
        if (gameMapOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        GameMap gameMap = gameMapOpt.get();
        List<GameSession> gameSessions = gameSessionService.findByGameMapId(gameMapId);
        if (gameSessions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        // Convertir les sessions en DTOs
        List<GameSessionDTO> gameSessionDTOs = new ArrayList<>();
        for (GameSession session : gameSessions) {
            GameSessionDTO gameSessionDTO = GameSessionDTO.fromEntity(session);
            gameSessionDTOs.add(gameSessionDTO);
        }
        return ResponseEntity.ok(gameSessionDTOs);
    }

    @GetMapping("/{id}/remaining-time")
    public ResponseEntity<Map<String, Object>> getRemainingTime(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        Optional<GameSession> sessionOpt = gameSessionService.findById(id);
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        GameSession session = sessionOpt.get();
        long remainingTime = session.getRemainingTimeInSeconds();
        boolean active = session.getActive() != null && session.getActive();

        response.put("remainingTimeInSeconds", remainingTime);
        response.put("active", active);

        logger.info("🕒 Temps restant pour session {}: {}s (active={})", id, remainingTime, active);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<List<GameSessionParticipantDTO>> getParticipants(@PathVariable Long id) {
        Optional<GameSession> sessionOpt = gameSessionService.findById(id);
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        GameSession session = sessionOpt.get();
        if (!session.getActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        // Convertir les participants en DTOs
        List<GameSessionParticipantDTO> participantDtos = new ArrayList<>();
        for (GameSessionParticipant participant : session.getParticipants()) {
            GameSessionParticipantDTO participantDTO = GameSessionParticipantDTO.fromEntity(participant);
            participantDtos.add(participantDTO);
        }
        return ResponseEntity.ok(participantDtos);
    }

    @GetMapping("/{id}/active-participants")
    public ResponseEntity<List<GameSessionParticipantDTO>> getActiveParticipants(@PathVariable Long id) {
        logger.info("🔍 Requête GET /api/game-sessions/{}/active-participants reçue", id);

        Optional<GameSession> sessionOpt = gameSessionService.findById(id);
        if (sessionOpt.isEmpty()) {
            logger.warn("⚠️ Session de jeu non trouvée pour l'ID={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        GameSession session = sessionOpt.get();
        logger.info("✅ Session trouvée : ID={}, active={}, nbParticipants={}",
                session.getId(), session.getActive(), session.getParticipants().size());

        if (!session.getActive()) {
            logger.warn("⛔ Session ID={} n'est pas active", id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        List<GameSessionParticipantDTO> activeParticipantDtos = new ArrayList<>();
        int compteur = 0;

        for (GameSessionParticipant participant : session.getParticipants()) {
            if (participant.getLeftAt() == null) {
                try {
                    GameSessionParticipantDTO dto = GameSessionParticipantDTO.fromEntity(participant);
                    activeParticipantDtos.add(dto);
                    logger.debug("👤 Participant ajouté : user={}, team={}, type={}",
                            dto.getUsername(),
                            dto.getTeamName(),
                            dto.getParticipantType()
                    );
                    compteur++;
                } catch (Exception e) {
                    logger.error("❌ Erreur de conversion participant ID={} en DTO", participant.getId(), e);
                }
            } else {
                logger.debug("🚪 Participant ID={} ignoré car a quitté la session à {}", participant.getId(), participant.getLeftAt());
            }
        }

        logger.info("📦 Total des participants actifs retournés pour session ID={}: {}", id, compteur);
        return ResponseEntity.ok(activeParticipantDtos);
    }


    @PostMapping("/{id}/participants")
    public ResponseEntity<GameSessionParticipantDTO> addParticipant(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload,
            Principal principal) {

        Long userId = Long.valueOf(payload.get("userId").toString());
        Long teamId = payload.get("teamId") != null ? Long.valueOf(payload.get("teamId").toString()) : null;
        Boolean isHost = payload.get("isHost") != null ? Boolean.valueOf(payload.get("isHost").toString()) : false;

        GameSessionParticipant participant = gameSessionService.addParticipant(id, userId, teamId, isHost);

        // Notifier tous les participants via WebSocket
        WebSocketMessage wsMessage = WebSocketMessage.participantJoined(participant, userId);
        messagingTemplate.convertAndSend("/topic/field/" + participant.getGameSession().getField().getId(), wsMessage);

        GameSessionParticipantDTO participantDTO = GameSessionParticipantDTO.fromEntity(participant);
        return ResponseEntity.status(HttpStatus.CREATED).body(participantDTO);
    }

    @DeleteMapping("/{id}/participants/{userId}")
    public ResponseEntity<Void> removeParticipant(@PathVariable Long id, @PathVariable Long userId) {
        GameSessionParticipant participant = gameSessionService.removeParticipant(id, userId);

        // Notifier tous les participants via WebSocket
        WebSocketMessage wsMessage = WebSocketMessage.participantLeft(participant, userId);
        messagingTemplate.convertAndSend("/topic/field/" + participant.getGameSession().getField().getId(), wsMessage);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/scenarios")
    public ResponseEntity<List<GameSessionScenarioDTO>> getScenarios(@PathVariable Long id) {
        List<GameSessionScenario> gameSessionScenario = gameSessionService.getScenarios(id);
        if (gameSessionScenario.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        // Convertir les scénarios en DTOs
        List<GameSessionScenarioDTO> scenarioDtos = new ArrayList<>();
        for (GameSessionScenario scenario : gameSessionScenario) {
            GameSessionScenarioDTO scenarioDTO = GameSessionScenarioDTO.fromEntity(scenario);
            scenarioDtos.add(scenarioDTO);
        }
        return ResponseEntity.ok(scenarioDtos);
    }

    @GetMapping("/{id}/active-scenarios")
    public ResponseEntity<List<GameSessionScenarioDTO>> getActiveScenarios(@PathVariable Long id) {
        List<GameSessionScenario> gameSessionScenario = gameSessionService.getActiveScenarios(id);
        if (gameSessionScenario.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        // Convertir les scénarios en DTOs
        List<GameSessionScenarioDTO> scenarioDtos = new ArrayList<>();
        for (GameSessionScenario scenario : gameSessionScenario) {
            GameSessionScenarioDTO scenarioDTO = GameSessionScenarioDTO.fromEntity(scenario);
            scenarioDtos.add(scenarioDTO);
        }
        return ResponseEntity.ok(scenarioDtos);
    }

    @PostMapping("/{id}/scenarios")
    public ResponseEntity<GameSessionScenarioDTO> addScenario(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {

        Long scenarioId = Long.valueOf(payload.get("scenarioId").toString());
        Boolean isMainScenario = payload.get("isMainScenario") != null ?
                Boolean.valueOf(payload.get("isMainScenario").toString()) : false;

        GameSessionScenario scenario = gameSessionService.addScenario(id, scenarioId, isMainScenario);

        // Notifier tous les participants via WebSocket
        WebSocketMessage wsMessage = WebSocketMessage.scenarioAdded(scenario, id, scenario.getScenario().getCreator().getId());
        messagingTemplate.convertAndSend("/topic/field/" + scenario.getGameSession().getField().getId(), wsMessage);

        GameSessionScenarioDTO scenarioDTO = GameSessionScenarioDTO.fromEntity(scenario);

        return ResponseEntity.status(HttpStatus.CREATED).body(scenarioDTO);
    }

    @PostMapping("/{id}/scenarios/{scenarioId}/activate")
    public ResponseEntity<GameSessionScenarioDTO> activateScenario(
            @PathVariable Long id,
            @PathVariable Long scenarioId) {

        GameSessionScenario scenario = gameSessionService.activateScenario(id, scenarioId);
        if (scenario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        // Notifier tous les participants via WebSocket
        WebSocketMessage wsMessage = WebSocketMessage.scenarioActivated(id, scenarioId, scenario.getScenario().getCreator().getId());
        messagingTemplate.convertAndSend("/topic/field/" + scenario.getGameSession().getField().getId(), wsMessage);

        GameSessionScenarioDTO scenarioDTO = GameSessionScenarioDTO.fromEntity(scenario);

        return ResponseEntity.ok(scenarioDTO);
    }

    @PostMapping("/{id}/scenarios/{scenarioId}/deactivate")
    public ResponseEntity<GameSessionScenarioDTO> deactivateScenario(
            @PathVariable Long id,
            @PathVariable Long scenarioId) {

        GameSessionScenario scenario = gameSessionService.deactivateScenario(id, scenarioId);

        // Notifier tous les participants via WebSocket
        WebSocketMessage wsMessage = WebSocketMessage.scenarioDeactivated(id, scenarioId, scenario.getScenario().getCreator().getId());
        messagingTemplate.convertAndSend("/topic/field/" + scenario.getGameSession().getField().getId(), wsMessage);

        GameSessionScenarioDTO scenarioDTO = GameSessionScenarioDTO.fromEntity(scenario);
        return ResponseEntity.ok(scenarioDTO);
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
            response.put("active", session.getActive());
            response.put("startTime", session.getStartTime());
            response.put("endTime", session.getEndTime());
        } else {
            response.put("status", "INACTIVE");
            response.put("active", false);
        }

        return ResponseEntity.ok(response);
    }


    @GetMapping("/current-session/{fieldId}")
    public ResponseEntity<GameSessionDTO> getGameSessionByFieldId(@PathVariable Long fieldId) {
        Optional<GameSession> sessionOpt = gameSessionService.findActiveSessionByFieldId(fieldId);

        if (sessionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        GameSession session = sessionOpt.get();
        if (!session.getActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        GameSessionDTO gameSessionDTO = GameSessionDTO.fromEntity(session);

        return ResponseEntity.ok(gameSessionDTO);
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
        response.put("active", session.getActive());
        response.put("startTime", session.getStartTime());
        response.put("endTime", session.getEndTime());

        return ResponseEntity.ok(response);
    }
}
