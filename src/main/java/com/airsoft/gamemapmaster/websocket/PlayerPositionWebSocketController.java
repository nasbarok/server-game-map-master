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
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

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

        // Construire le message structuré
        WebSocketMessage webSocketMessage = WebSocketMessage.playerPosition(position, message.getSenderId());

        // Aperçu JSON complet
        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonPreview = mapper.writeValueAsString(webSocketMessage);
            logger.info("📤 Message WebSocket prêt à envoyer sur /topic/field/{} : {}", fieldId, jsonPreview);
        } catch (JsonProcessingException e) {
            logger.warn("❌ Impossible de sérialiser le message WebSocket", e);
        }

        // Sauvegarde en base
        playerPositionService.savePosition(position);

        // Envoi WebSocket
        messagingTemplate.convertAndSend("/topic/field/" + fieldId, webSocketMessage);
    }
}
