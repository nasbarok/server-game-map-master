package com.airsoft.gamemapmaster.scenario.treasurehunt.websocket;

import com.airsoft.gamemapmaster.scenario.treasurehunt.model.TreasureFound;
import com.airsoft.gamemapmaster.scenario.treasurehunt.model.TreasureHuntNotification;
import com.airsoft.gamemapmaster.websocket.WebSocketMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TreasureHuntWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(TreasureHuntWebSocketHandler.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Notifie tous les clients connectés qu'un trésor a été trouvé
     */
    public void notifyTreasureFound(TreasureFound treasureFound, String username, String teamName,
                                    int points, int totalScore, boolean isNewLeader,long senderId,long gameSessionId) {
        try {
            Long scenarioId = treasureFound.getTreasure().getTreasureHuntScenario().getScenario().getId();

            WebSocketMessage notification = TreasureHuntNotification.treasureFound(
                    treasureFound, username, teamName, points, totalScore, isNewLeader,senderId, gameSessionId);

            messagingTemplate.convertAndSend("/topic/field/" + gameSessionId, notification);

            logger.info("Notification envoyée: trésor trouvé par {} ({})", username, teamName);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification de trésor trouvé", e);
        }
    }

    /**
     * Met à jour le tableau des scores pour tous les clients connectés
     */
    public void updateScoreboard(Long scenarioId, Map<String, Object> scoreboard) {
        try {
            TreasureHuntNotification notification = TreasureHuntNotification.scoreboardUpdate(scoreboard);

            messagingTemplate.convertAndSend("/topic/game/" + scenarioId, notification);

            logger.info("Notification envoyée: mise à jour du tableau des scores pour le scénario {}", scenarioId);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la mise à jour du tableau des scores", e);
        }
    }

    /**
     * Envoie une notification d'événement de jeu à tous les clients connectés
     */
    public void notifyGameEvent(Long scenarioId, String eventType, String message) {
        try {
            TreasureHuntNotification notification = new TreasureHuntNotification();
            notification.setType(eventType);
            notification.setMessage(message);

            messagingTemplate.convertAndSend("/topic/game/" + scenarioId, notification);

            logger.info("Notification envoyée: événement {} pour le scénario {}", eventType, scenarioId);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification d'événement de jeu", e);
        }
    }
}
