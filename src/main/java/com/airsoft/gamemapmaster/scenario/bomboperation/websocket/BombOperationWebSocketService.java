package com.airsoft.gamemapmaster.scenario.bomboperation.websocket;

import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombOperationNotification;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombOperationState;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombOperationTeam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
public class BombOperationWebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(BombOperationWebSocketService.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Envoie une notification de début de round
     * @param sessionId ID de la session
     * @param roundNumber Numéro du round
     * @param attackTeamScore Score de l'équipe d'attaque
     * @param defenseTeamScore Score de l'équipe de défense
     */
    public void sendRoundStartNotification(Long sessionId, Integer roundNumber, Integer attackTeamScore, Integer defenseTeamScore) {
        logger.info("Envoi d'une notification de début de round {} pour la session ID: {}", roundNumber, sessionId);

        BombOperationNotification notification = new BombOperationNotification();
        notification.setType("ROUND_START");
        notification.setSessionId(sessionId);
        notification.setRoundNumber(roundNumber);
        notification.setAttackTeamScore(attackTeamScore);
        notification.setDefenseTeamScore(defenseTeamScore);
        notification.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));

        sendNotification(sessionId, notification);
    }

    /**
     * Envoie une notification de pose de bombe
     * @param sessionId ID de la session
     * @param userId ID de l'utilisateur qui a posé la bombe
     * @param siteId ID du site de bombe
     * @param siteName Nom du site de bombe
     * @param bombTimer Durée du timer de la bombe en secondes
     */
    public void sendBombPlantedNotification(Long sessionId, Long userId, Long siteId, String siteName, Integer bombTimer) {
        logger.info("Envoi d'une notification de pose de bombe par l'utilisateur ID: {} sur le site ID: {} pour la session ID: {}",
                userId, siteId, sessionId);

        BombOperationNotification notification = new BombOperationNotification();
        notification.setType("BOMB_PLANTED");
        notification.setSessionId(sessionId);
        notification.setUserId(userId);
        notification.setSiteId(siteId);
        notification.setSiteName(siteName);
        notification.setBombTimer(bombTimer);
        notification.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));

        sendNotification(sessionId, notification);
    }

    /**
     * Envoie une notification de début de désamorçage
     * @param sessionId ID de la session
     * @param userId ID de l'utilisateur qui désamorce
     * @param defuseTime Durée du désamorçage en secondes
     */
    public void sendDefuseStartNotification(Long sessionId, Long userId, Integer defuseTime) {
        logger.info("Envoi d'une notification de début de désamorçage par l'utilisateur ID: {} pour la session ID: {}",
                userId, sessionId);

        BombOperationNotification notification = new BombOperationNotification();
        notification.setType("DEFUSE_START");
        notification.setSessionId(sessionId);
        notification.setUserId(userId);
        notification.setDefuseTime(defuseTime);
        notification.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));

        sendNotification(sessionId, notification);
    }

    /**
     * Envoie une notification de fin de désamorçage
     * @param sessionId ID de la session
     * @param userId ID de l'utilisateur qui a désamorcé
     */
    public void sendDefuseSuccessNotification(Long sessionId, Long userId) {
        logger.info("Envoi d'une notification de désamorçage réussi par l'utilisateur ID: {} pour la session ID: {}",
                userId, sessionId);

        BombOperationNotification notification = new BombOperationNotification();
        notification.setType("DEFUSE_SUCCESS");
        notification.setSessionId(sessionId);
        notification.setUserId(userId);
        notification.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));

        sendNotification(sessionId, notification);
    }

    /**
     * Envoie une notification d'explosion de bombe
     * @param sessionId ID de la session
     */
    public void sendBombExplodedNotification(Long sessionId) {
        logger.info("Envoi d'une notification d'explosion de bombe pour la session ID: {}", sessionId);

        BombOperationNotification notification = new BombOperationNotification();
        notification.setType("BOMB_EXPLODED");
        notification.setSessionId(sessionId);
        notification.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));

        sendNotification(sessionId, notification);
    }

    /**
     * Envoie une notification de fin de round
     * @param sessionId ID de la session
     * @param roundNumber Numéro du round
     * @param winnerTeam Équipe gagnante
     * @param reason Raison de la victoire
     * @param attackTeamScore Score de l'équipe d'attaque
     * @param defenseTeamScore Score de l'équipe de défense
     */
    public void sendRoundEndNotification(Long sessionId, Integer roundNumber, BombOperationTeam winnerTeam,
                                         String reason, Integer attackTeamScore, Integer defenseTeamScore) {
        logger.info("Envoi d'une notification de fin de round {} pour la session ID: {} avec victoire de l'équipe: {}",
                roundNumber, sessionId, winnerTeam);

        BombOperationNotification notification = new BombOperationNotification();
        notification.setType("ROUND_END");
        notification.setSessionId(sessionId);
        notification.setRoundNumber(roundNumber);
        notification.setWinnerTeam(winnerTeam.toString());
        notification.setReason(reason);
        notification.setAttackTeamScore(attackTeamScore);
        notification.setDefenseTeamScore(defenseTeamScore);
        notification.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));

        sendNotification(sessionId, notification);
    }

    /**
     * Envoie une notification de fin de partie
     * @param sessionId ID de la session
     * @param winnerTeam Équipe gagnante
     * @param attackTeamScore Score final de l'équipe d'attaque
     * @param defenseTeamScore Score final de l'équipe de défense
     */
    public void sendGameEndNotification(Long sessionId, BombOperationTeam winnerTeam,
                                        Integer attackTeamScore, Integer defenseTeamScore) {
        logger.info("Envoi d'une notification de fin de partie pour la session ID: {} avec victoire de l'équipe: {}",
                sessionId, winnerTeam);

        BombOperationNotification notification = new BombOperationNotification();
        notification.setType("GAME_END");
        notification.setSessionId(sessionId);
        notification.setWinnerTeam(winnerTeam.toString());
        notification.setAttackTeamScore(attackTeamScore);
        notification.setDefenseTeamScore(defenseTeamScore);
        notification.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));

        sendNotification(sessionId, notification);
    }

    /**
     * Envoie une notification de changement d'état de joueur
     * @param sessionId ID de la session
     * @param userId ID de l'utilisateur
     * @param isAlive État vivant/mort du joueur
     */
    public void sendPlayerStateChangeNotification(Long sessionId, Long userId, Boolean isAlive) {
        logger.info("Envoi d'une notification de changement d'état du joueur ID: {} (vivant: {}) pour la session ID: {}",
                userId, isAlive, sessionId);

        BombOperationNotification notification = new BombOperationNotification();
        notification.setType("PLAYER_STATE_CHANGE");
        notification.setSessionId(sessionId);
        notification.setUserId(userId);
        notification.setIsAlive(isAlive);
        notification.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));

        sendNotification(sessionId, notification);
    }

    /**
     * Envoie une notification de changement d'état de jeu
     * @param sessionId ID de la session
     * @param gameState Nouvel état de jeu
     */
    public void sendGameStateChangeNotification(Long sessionId, BombOperationState gameState) {
        logger.info("Envoi d'une notification de changement d'état de jeu ({}) pour la session ID: {}",
                gameState, sessionId);

        BombOperationNotification notification = new BombOperationNotification();
        notification.setType("GAME_STATE_CHANGE");
        notification.setSessionId(sessionId);
        notification.setGameState(gameState.toString());
        notification.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));

        sendNotification(sessionId, notification);
    }

    /**
     * Envoie une notification de mise à jour du timer
     * @param sessionId ID de la session
     * @param remainingTime Temps restant en secondes
     */
    public void sendTimerUpdateNotification(Long sessionId, Integer remainingTime) {
        // Pas de log pour éviter de spammer les logs avec les mises à jour fréquentes du timer

        BombOperationNotification notification = new BombOperationNotification();
        notification.setType("TIMER_UPDATE");
        notification.setSessionId(sessionId);
        notification.setRemainingTime(remainingTime);
        notification.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));

        sendNotification(sessionId, notification);
    }

    /**
     * Envoie une notification générique
     * @param sessionId ID de la session
     * @param notification Notification à envoyer
     */
    private void sendNotification(Long sessionId, BombOperationNotification notification) {
        String destination = "/topic/bomb-operation/" + sessionId;
        messagingTemplate.convertAndSend(destination, notification);
    }
}
