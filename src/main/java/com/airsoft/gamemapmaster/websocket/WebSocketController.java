package com.airsoft.gamemapmaster.websocket;

import com.airsoft.gamemapmaster.controller.GameMapController;
import com.airsoft.gamemapmaster.model.ConnectedPlayer;
import com.airsoft.gamemapmaster.model.Field;
import com.airsoft.gamemapmaster.model.Team;
import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Contrôleur pour gérer les messages WebSocket
 */
@Controller
public class WebSocketController {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private ConnectedPlayerService connectedPlayerService;

    @Autowired
    private FieldUserHistoryService fieldUserHistoryService;

    @Autowired
    private FieldService fieldService;
    /**
     * Gère les messages envoyés à /app/message
     * Diffuse le message à tous les abonnés de /topic/public
     */
    @MessageMapping("/message")
    public void processMessage(@Payload WebSocketMessage message, Principal principal) {
        if (principal != null) {
            User user = userService.findByUsername(principal.getName()).orElse(null);
            message.setSenderId(user.getId());
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
                String.valueOf(message.getSenderId()), "/queue/private", message);
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
    public void handleInvitation(@Payload Map<String, Object> rawMessage, Principal principal) {


        Long senderId =  Long.valueOf(rawMessage.get("senderId").toString());
        Long targetUserId = Long.valueOf(rawMessage.get("targetUserId").toString());
        Long fieldId = Long.valueOf(rawMessage.get("fieldId").toString());

        User targetUser = userService.findById(targetUserId).orElse(null);
        if (targetUser == null){
            logger.warn("⚠️ Utilisateur non trouvé pour l'invitation");
            return;
        }
        User senderUser = userService.findById(senderId).orElse(null);

        Field field = fieldService.findById(fieldId).orElse(null);

        // Recréer le message à envoyer au joueur ciblé
        Map<String, Object> invitationData = new HashMap<>();
        invitationData.put("fieldId", fieldId);
        invitationData.put("senderId", senderId);
        invitationData.put("targetUserId", targetUserId);
        invitationData.put("fromUsername", senderUser.getUsername());
        invitationData.put("mapName", field.getName());

        WebSocketMessage invitationMessage = new WebSocketMessage(
                "INVITATION_RECEIVED",
                invitationData,
                senderId,
                System.currentTimeMillis()
        );

        logger.info("📩 Envoi d'une invitation de {} à {}", senderId, targetUserId);

        // Envoi vers le canal du joueur cible
        messagingTemplate.convertAndSend("/topic/user/" + targetUserId, invitationMessage);
    }

    @MessageMapping("/invitation-response")
    public void handleInvitationResponse(@Payload WebSocketMessage message) {
        Map<String, Object> payload = (Map<String, Object>) message.getPayload();
        Long fromUserId = Long.valueOf(payload.get("senderId").toString());
        Long toUserId = Long.valueOf(payload.get("targetUserId").toString());
        Long fieldId = Long.valueOf(payload.get("fieldId").toString());
        boolean accepted = Boolean.parseBoolean(payload.get("accepted").toString());

        // ✅ Récupération du joueur
        Optional<User> fromUser = userService.findById(fromUserId);
        String fromUsername = fromUser.map(User::getUsername).orElse("unknown");

        // ✅ Si accepté, connecter le joueur au field s'il ne l'est pas déjà
        if (accepted) {
            boolean alreadyConnected = connectedPlayerService.isPlayerConnectedToField(fieldId, fromUserId);
            if (!alreadyConnected) {
                logger.info("📡 Connexion du joueur {} au terrain {}", fromUserId, fieldId);
                ConnectedPlayer connectedPlayer = connectedPlayerService.connectPlayerToField(fieldId, fromUserId, null); // Pas d'équipe au départ
                // 🔹 Ajout dans l'historique de connexion
                fieldUserHistoryService.logJoin(fromUserId,fieldId);

                Map<String, Object> playerConnectedPayload = new HashMap<>();
                playerConnectedPayload.put("playerId", connectedPlayer.getUser().getId());
                playerConnectedPayload.put("senderId", fromUserId);
                playerConnectedPayload.put("playerUsername", connectedPlayer.getUser().getUsername());
                playerConnectedPayload.put("fieldId", fieldId);

                WebSocketMessage playerConnectedMessage = new WebSocketMessage(
                        "PLAYER_CONNECTED",
                        playerConnectedPayload,
                        fromUserId,
                        System.currentTimeMillis()
                );
                // 🔹 Envoi du message à tous les utilisateurs connectés au terrain
                messagingTemplate.convertAndSend("/topic/field/" + fieldId, playerConnectedMessage);
            }
        }
        String mapName = (String) payload.getOrDefault("mapName", "Unknown Map");
        Map<String, Object> responsePayload = new HashMap<>();
        responsePayload.put("fieldId", fieldId);
        responsePayload.put("senderId", fromUserId);
        responsePayload.put("targetUserId", toUserId);
        responsePayload.put("fromUsername", fromUsername);
        responsePayload.put("mapName", mapName);
        responsePayload.put("accepted", accepted);

        WebSocketMessage responseMessage = new WebSocketMessage(
                "INVITATION_RESPONSE",
                responsePayload,
                fromUserId,
                System.currentTimeMillis()
        );

        messagingTemplate.convertAndSend("/topic/user/" + toUserId, responseMessage);
    }

    @MessageMapping("/leave-map")
    public void handlePlayerLeave(@Payload WebSocketMessage message) {
        Map<String, Object> payload = (Map<String, Object>) message.getPayload();
        Long userId = Long.valueOf(payload.get("userId").toString());
        Long mapId = Long.valueOf(payload.get("mapId").toString());

        logger.info("📤 Le joueur {} quitte la carte {}", userId, mapId);

        boolean disconnected = connectedPlayerService.disconnectPlayerFromMap(mapId, userId);
        if (disconnected) {
            logger.info("🔚 Joueur {} déconnecté de la carte {}", userId, mapId);
            fieldUserHistoryService.logLeave(mapId, userId);
        } else {
            logger.warn("⚠️ Impossible de déconnecter le joueur {} de la carte {}", userId, mapId);
        }
    }
}
