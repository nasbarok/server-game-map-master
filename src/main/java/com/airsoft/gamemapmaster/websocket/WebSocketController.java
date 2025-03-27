package com.airsoft.gamemapmaster.websocket;

import com.airsoft.gamemapmaster.controller.GameMapController;
import com.airsoft.gamemapmaster.model.Team;
import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.service.ConnectedPlayerService;
import com.airsoft.gamemapmaster.service.FieldUserHistoryService;
import com.airsoft.gamemapmaster.service.TeamService;
import com.airsoft.gamemapmaster.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
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
        Long fromUserId = Long.valueOf(payload.get("fromUserId").toString());
        Long toUserId = Long.valueOf(payload.get("toUserId").toString());
        Long mapId = Long.valueOf(payload.get("mapId").toString());
        boolean accepted = Boolean.parseBoolean(payload.get("accepted").toString());

        // ✅ Récupération du joueur
        Optional<User> fromUser = userService.findById(fromUserId);

        // ✅ Si accepté, connecter le joueur à la carte s'il ne l'est pas déjà
        if (accepted) {
            boolean alreadyConnected = connectedPlayerService.isPlayerConnectedToMap(mapId, fromUserId);
            if (!alreadyConnected) {
                logger.info("📡 Connexion du joueur {} à la carte {}", fromUserId, mapId);
                connectedPlayerService.connectPlayerToMap(mapId, fromUserId, null); // Pas d'équipe au départ
                // 🔹 Ajout dans l'historique de connexion
                fieldUserHistoryService.logJoin(mapId, fromUserId);
            }
        }

        // ✅ Récupération de l'équipe via ConnectedPlayerRepository
        Team team = connectedPlayerService.findTeamByUserAndMap(fromUserId, mapId);

        // ✅ Enrichissement du message
        fromUser.ifPresent(user -> payload.put("fromUsername", user.getUsername()));

        if (team != null) {
            payload.put("teamId", team.getId());
            payload.put("teamName", team.getName());
        }

        WebSocketMessage responseMessage = new WebSocketMessage(
                "INVITATION_RESPONSE",
                payload,
                payload.get("fromUserId").toString(),
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

    @MessageMapping("/team-update")
    public void handleTeamUpdate(@Payload WebSocketMessage message) {
        Map<String, Object> payload = (Map<String, Object>) message.getPayload();
        Long mapId = Long.valueOf(payload.get("mapId").toString());

        // Diffuser à tous les joueurs connectés à cette carte
        messagingTemplate.convertAndSend("/topic/map/" + mapId, message);
    }
}
