package com.airsoft.gamemapmaster.websocket;

import com.airsoft.gamemapmaster.controller.GameSessionController;
import com.airsoft.gamemapmaster.position.dto.PlayerPositionDTO;
import com.airsoft.gamemapmaster.position.dto.WebSocketMessageDTO;
import com.airsoft.gamemapmaster.position.service.PlayerPositionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contrôleur WebSocket pour la gestion des positions des joueurs.
 * Utilise le topic centralisé /topic/field/{fieldId} pour toutes les communications.
 */
@Controller
@RequiredArgsConstructor
public class PlayerPositionWebSocketController {
    private static final Logger logger = LoggerFactory.getLogger(GameSessionController.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final PlayerPositionService playerPositionService;
    private final Map<Integer, Instant> lastSavedTimestamps = new ConcurrentHashMap<>();
    private static final Duration SAVE_INTERVAL = Duration.ofSeconds(30);
    /**
     * Reçoit une position de joueur et la diffuse à tous les clients abonnés au topic du terrain.
     * La position est également enregistrée en base de données.
     *
     * @param fieldId ID du terrain
     * @param message DTO contenant les informations de position
     */
    @MessageMapping("/field/{fieldId}")
    public void handlePlayerPosition(@DestinationVariable Integer fieldId, WebSocketMessageDTO message) {
        PlayerPositionDTO position = message.getPayload();
        position.setUserId(message.getSenderId());

        logger.info("📥 [handlePlayerPosition] Reçu message WebSocket pour fieldId={} et senderId={}", fieldId, message.getSenderId());
        logger.info("↪️ Position reçue : latitude={}, longitude={}, gameSessionId={}, teamId={}, timestamp={}",
                position.getLatitude(),
                position.getLongitude(),
                position.getGameSessionId(),
                position.getTeamId(),
                position.getTimestamp());

        // Construire le message WebSocket à renvoyer
        WebSocketMessage webSocketMessage = WebSocketMessage.playerPosition(position, message.getSenderId());

        // 🔄 Décider si on enregistre ou pas en base
        Instant now = Instant.now();
        Instant lastSaved = playerPositionService.getLastSavedTimestamp(position.getUserId()).orElse(Instant.EPOCH);

        if (Duration.between(lastSaved, now).compareTo(SAVE_INTERVAL) >= 0) {
            playerPositionService.savePosition(position);
            lastSavedTimestamps.put(Math.toIntExact(position.getUserId()), now);
            logger.info("💾 [handlePlayerPosition] Position SAUVEGARDÉE pour userId={}", position.getUserId());
        } else {
            logger.debug("⏱️ [handlePlayerPosition] Position ignorée (déjà sauvegardée récemment)");
        }

        // Envoi aux abonnés WebSocket
        messagingTemplate.convertAndSend("/topic/field/" + fieldId, webSocketMessage);
        logger.info("📤 [handlePlayerPosition] Position envoyée pour le fieldId={} et senderId={}", fieldId, message.getSenderId());
    }

}
