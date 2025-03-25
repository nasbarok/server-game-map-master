package com.airsoft.gamemapmaster.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

/**
 * Contrôleur pour gérer les messages WebSocket
 */
@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Gère les messages envoyés à /app/message
     * Diffuse le message à tous les abonnés de /topic/public
     */
    @MessageMapping("/message")
    public void processMessage(@Payload WebSocketMessage message, Principal principal) {
        if (principal != null) {
            message.setSender(principal.getName());
        }
        messagingTemplate.convertAndSend("/topic/public", message);
    }

    /**
     * Gère les messages envoyés à /app/private-message
     * Envoie le message à un utilisateur spécifique
     */
    @MessageMapping("/private-message")
    public void sendPrivateMessage(@Payload WebSocketMessage message) {
        messagingTemplate.convertAndSendToUser(
                message.getSender(), "/queue/private", message);
    }

    /**
     * Gère les messages envoyés à /app/scenario
     * Diffuse les mises à jour de scénario à tous les abonnés de /topic/scenario/{scenarioId}
     */
    @MessageMapping("/scenario")
    public void updateScenario(@Payload WebSocketMessage message) {
        Long scenarioId = Long.valueOf(message.getPayload().toString());
        messagingTemplate.convertAndSend("/topic/scenario/" + scenarioId, message);
    }

    @MessageMapping("/invitation")
    public void handleInvitation(@Payload WebSocketMessage message, Principal principal) {
        // Tu peux récupérer l'identité de l'expéditeur ici
        if (principal != null) {
            message.setSender(principal.getName());
        }

        // Extraire les infos nécessaires de payload
        Map<String, Object> data = (Map<String, Object>) message.getPayload();
        Long toUserId = Long.valueOf(data.get("toUserId").toString());

        // Recréer le message structuré
        WebSocketMessage invitationMessage = new WebSocketMessage(
                "INVITATION_RECEIVED",
                data,
                message.getSender(),
                System.currentTimeMillis()
        );

        // Envoyer sur le bon canal
        messagingTemplate.convertAndSend("/topic/user/" + toUserId, invitationMessage);
    }
    @MessageMapping("/invitation-response")
    public void handleInvitationResponse(@Payload WebSocketMessage message) {
        Map<String, Object> payload = (Map<String, Object>) message.getPayload();
        Long toUserId = Long.valueOf(payload.get("toUserId").toString());

        WebSocketMessage responseMessage = new WebSocketMessage(
                "INVITATION_RESPONSE",
                payload,
                payload.get("fromUserId").toString(),
                System.currentTimeMillis()
        );

        messagingTemplate.convertAndSend("/topic/user/" + toUserId, responseMessage);
    }
}
