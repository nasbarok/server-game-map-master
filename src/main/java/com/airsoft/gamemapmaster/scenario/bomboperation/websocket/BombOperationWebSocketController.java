package com.airsoft.gamemapmaster.scenario.bomboperation.websocket;

import com.airsoft.gamemapmaster.scenario.bomboperation.dto.BombOperationActionDTO;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombOperationNotification;
import com.airsoft.gamemapmaster.scenario.bomboperation.service.BombOperationPlayerStateService;
import com.airsoft.gamemapmaster.scenario.bomboperation.service.BombOperationSessionService;
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
     * Endpoint pour recevoir les demandes de pose de bombe
     * @param sessionId ID de la session
     * @param message Message contenant les informations de pose de bombe
     * @return Notification de pose de bombe
     */
    @MessageMapping("/bomb-operation/{sessionId}/plant-bomb")
    @SendTo("/topic/bomb-operation/{sessionId}")
    public BombOperationNotification handlePlantBomb(
            @DestinationVariable Long sessionId,
            PlantBombMessage message) {

        logger.info("Réception d'une demande de pose de bombe pour l'utilisateur ID: {} sur le site ID: {} dans la session ID: {}",
                message.getUserId(), message.getSiteId(), sessionId);

        try {
            // Tenter de poser la bombe
            bombOperationService.plantBomb(sessionId, message.getUserId(), message.getSiteId(),
                    message.getLatitude(), message.getLongitude());

            // La notification sera envoyée par le service de session via le WebSocketService
            // Retourner null pour ne pas envoyer de notification supplémentaire
            return null;
        } catch (Exception e) {
            logger.error("Erreur lors de la pose de bombe: {}", e.getMessage());

            // Créer une notification d'erreur
            BombOperationNotification notification = new BombOperationNotification();
            notification.setType("ERROR");
            notification.setSessionId(sessionId);
            notification.setUserId(message.getUserId());
            notification.setMessage("Erreur lors de la pose de bombe: " + e.getMessage());

            return notification;
        }
    }

    /**
     * Endpoint pour recevoir les demandes de désamorçage de bombe
     * @param sessionId ID de la session
     * @param message Message contenant les informations de désamorçage
     * @return Notification de début de désamorçage
     */
    @MessageMapping("/bomb-operation/{sessionId}/start-defusing")
    @SendTo("/topic/bomb-operation/{sessionId}")
    public BombOperationNotification handleStartDefusing(
            @DestinationVariable Long sessionId,
            DefuseMessage message) {

        logger.info("Réception d'une demande de désamorçage pour l'utilisateur ID: {} dans la session ID: {}",
                message.getUserId(), sessionId);

        try {
            // Tenter de commencer le désamorçage
            bombOperationService.startDefusing(sessionId, message.getUserId(),
                    message.getLatitude(), message.getLongitude());

            // La notification sera envoyée par le service de session via le WebSocketService
            // Retourner null pour ne pas envoyer de notification supplémentaire
            return null;
        } catch (Exception e) {
            logger.error("Erreur lors du début de désamorçage: {}", e.getMessage());

            // Créer une notification d'erreur
            BombOperationNotification notification = new BombOperationNotification();
            notification.setType("ERROR");
            notification.setSessionId(sessionId);
            notification.setUserId(message.getUserId());
            notification.setMessage("Erreur lors du début de désamorçage: " + e.getMessage());

            return notification;
        }
    }

    /**
     * Endpoint pour recevoir les demandes de fin de désamorçage
     * @param sessionId ID de la session
     * @param message Message contenant les informations de fin de désamorçage
     * @return Notification de fin de désamorçage
     */
    @MessageMapping("/bomb-operation/{sessionId}/finish-defusing")
    @SendTo("/topic/bomb-operation/{sessionId}")
    public BombOperationNotification handleFinishDefusing(
            @DestinationVariable Long sessionId,
            DefuseMessage message) {

        logger.info("Réception d'une demande de fin de désamorçage pour l'utilisateur ID: {} dans la session ID: {}",
                message.getUserId(), sessionId);

        try {
            // Tenter de terminer le désamorçage
            bombOperationService.finishDefusing(sessionId, message.getUserId());

            // La notification sera envoyée par le service de session via le WebSocketService
            // Retourner null pour ne pas envoyer de notification supplémentaire
            return null;
        } catch (Exception e) {
            logger.error("Erreur lors de la fin de désamorçage: {}", e.getMessage());

            // Créer une notification d'erreur
            BombOperationNotification notification = new BombOperationNotification();
            notification.setType("ERROR");
            notification.setSessionId(sessionId);
            notification.setUserId(message.getUserId());
            notification.setMessage("Erreur lors de la fin de désamorçage: " + e.getMessage());

            return notification;
        }
    }


    /**
     * Reçoit une action du scénario Opération Bombe et la traite.
     *
     * @param fieldId ID du terrain
     * @param actionDTO DTO contenant les informations de l'action
     */
    @MessageMapping("/field/{fieldId}/bomb")
    public void handleBombOperationAction(
            @DestinationVariable Integer fieldId,
            BombOperationActionDTO actionDTO) {

        if (!"BOMB_OPERATION_ACTION".equals(actionDTO.getType())) {
            return; // Ignore les autres types
        }

        Long gameSessionId = actionDTO.getGameSessionId();
        Long senderId = actionDTO.getSenderId();
        var payload = actionDTO.getPayload();

        try {
            switch (actionDTO.getAction()) {

                case "PLANT_BOMB":
                    bombOperationService.plantBomb(
                            gameSessionId,
                            senderId,
                            ((Number) payload.get("bombSiteId")).longValue(),
                            ((Number) payload.get("latitude")).doubleValue(),
                            ((Number) payload.get("longitude")).doubleValue()
                    );
                    break;

                case "START_DEFUSE_BOMB":
                    bombOperationService.startDefusing(
                            gameSessionId,
                            senderId,
                            ((Number) payload.get("latitude")).doubleValue(),
                            ((Number) payload.get("longitude")).doubleValue()
                    );
                    break;

                case "END_DEFUSE_BOMB":
                    bombOperationService.finishDefusing(
                            gameSessionId,
                            senderId
                    );
                    break;

                default:
                    logger.warn("❌ Action non reconnue: {}", actionDTO.getAction());
            }

            // Envoyer la mise à jour d'état à tous les clients
            messagingTemplate.convertAndSend(
                    "/topic/field/" + fieldId,
                    bombOperationService.getGameSessionState(gameSessionId)
            );

        } catch (Exception e) {
            logger.error("❌ Erreur lors du traitement de l'action Bombe: {}", e.getMessage());
/*            messagingTemplate.convertAndSend(
                    "/topic/field/" + fieldId,
                    BombOperationNotification.error(gameSessionId, senderId, e.getMessage())
            );*/
        }
    }


    /**
     * Classe interne pour les messages de mise à jour de position
     */
    public static class PositionUpdateMessage {
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

    /**
     * Classe interne pour les messages de joueur tué
     */
    public static class PlayerKilledMessage {
        private Long userId;
        private Long killerUserId;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getKillerUserId() {
            return killerUserId;
        }

        public void setKillerUserId(Long killerUserId) {
            this.killerUserId = killerUserId;
        }
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
