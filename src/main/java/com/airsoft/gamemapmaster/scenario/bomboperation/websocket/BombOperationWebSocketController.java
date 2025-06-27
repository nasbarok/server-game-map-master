package com.airsoft.gamemapmaster.scenario.bomboperation.websocket;

import com.airsoft.gamemapmaster.scenario.bomboperation.dto.BombOperationActionDTO;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombOperationNotification;
import com.airsoft.gamemapmaster.scenario.bomboperation.service.BombOperationPlayerStateService;
import com.airsoft.gamemapmaster.scenario.bomboperation.service.BombOperationSessionService;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class BombOperationWebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(BombOperationWebSocketController.class);

    @Autowired
    private BombOperationSessionService bombOperationService;

    @Autowired
    private BombOperationPlayerStateService playerStateService;

    @Autowired
    private BombOperationWebSocketService webSocketService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    /**
     * Endpoint pour recevoir les mises à jour de position des joueurs
     * @param sessionId ID de la session
     * @param message Message contenant les informations de position
     * @return Notification de mise à jour de position
     */
    @MessageMapping("/bomb-operation/{sessionId}/position")
    @SendTo("/topic/bomb-operation/{sessionId}")
    public BombOperationNotification handlePositionUpdate(
            @DestinationVariable Long sessionId,
            PositionUpdateMessage message) {

        logger.info("Réception d'une mise à jour de position pour l'utilisateur ID: {} dans la session ID: {}",
                message.getUserId(), sessionId);

        // Vérifier si le joueur est dans un site de bombe actif
        boolean isInActiveSite = bombOperationService.isPlayerInActiveBombSite(
                sessionId, message.getLatitude(), message.getLongitude()) != null;

        // Créer une notification de mise à jour de position
        BombOperationNotification notification = new BombOperationNotification();
        notification.setType("POSITION_UPDATE");
        notification.setSessionId(sessionId);
        notification.setUserId(message.getUserId());
        notification.setLatitude(message.getLatitude());
        notification.setLongitude(message.getLongitude());
        notification.setIsInActiveSite(isInActiveSite);

        return notification;
    }

    /**
     * Endpoint pour recevoir les notifications de joueur tué
     * @param sessionId ID de la session
     * @param message Message contenant les informations du joueur tué
     * @return Notification de joueur tué
     */
    @MessageMapping("/bomb-operation/{sessionId}/player-killed")
    @SendTo("/topic/bomb-operation/{sessionId}")
    public BombOperationNotification handlePlayerKilled(
            @DestinationVariable Long sessionId,
            PlayerKilledMessage message) {

        logger.info("Réception d'une notification de joueur tué pour l'utilisateur ID: {} dans la session ID: {}",
                message.getUserId(), sessionId);

        // Mettre à jour l'état du joueur
        playerStateService.killPlayer(sessionId, message.getUserId());

        // Créer une notification de joueur tué
        BombOperationNotification notification = new BombOperationNotification();
        notification.setType("PLAYER_KILLED");
        notification.setSessionId(sessionId);
        notification.setUserId(message.getUserId());
        notification.setKillerUserId(message.getKillerUserId());

        return notification;
    }


    /**
     * Classe interne pour les messages de mise à jour de position
     */
    @Setter
    @Getter
    public static class PositionUpdateMessage {
        private Long userId;
        private Double latitude;
        private Double longitude;

    }

    /**
     * Classe interne pour les messages de joueur tué
     */
    @Setter
    @Getter
    public static class PlayerKilledMessage {
        private Long userId;
        private Long killerUserId;

    }

    /**
     * Classe interne pour les messages de pose de bombe
     */
    public static class PlantBombMessage {
        private Long userId;
        private Long siteId;
        private Double latitude;
        private Double longitude;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getSiteId() {
            return siteId;
        }

        public void setSiteId(Long siteId) {
            this.siteId = siteId;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }
    }

    /**
     * Classe interne pour les messages de désamorçage
     */
    public static class DefuseMessage {
        private Long userId;
        private Double latitude;
        private Double longitude;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }
    }
}
