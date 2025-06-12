package com.airsoft.gamemapmaster.scenario.bomboperation.websocket;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombOperationSession;
import com.airsoft.gamemapmaster.websocket.WebSocketMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class BombOperationWebSocketNotifier {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendToGameSession(Long gameSessionId, WebSocketMessage message) {
        messagingTemplate.convertAndSend("/topic/field/" + gameSessionId, message);
    }

    /**
     * Envoie une notification de bombe armée (version simplifiée)
     */
    public void sendBombPlantedNotification(Long fieldId, Long gameSessionId, Long userId, Long siteId, String siteName, Integer bombTimer) {
        Map<String, Object> data = new HashMap<>();
        data.put("gameSessionId", gameSessionId);
        data.put("fieldId", fieldId);
        data.put("userId", userId);
        data.put("siteId", siteId);
        data.put("siteName", siteName);
        data.put("bombTimer", bombTimer);
        data.put("timestamp", System.currentTimeMillis());

        WebSocketMessage message = new WebSocketMessage();
        message.setType("BOMB_PLANTED");
        message.setSenderId(userId);
        message.setPayload(data);

        sendToGameSession(fieldId, message);
    }

    /**
     * Envoie une notification de bombe désarmée (version simplifiée)
     */
    public void sendDefuseSuccessNotification(Long fieldId, Long gameSessionId, Long userId, Long siteId, String siteName) {
        Map<String, Object> data = new HashMap<>();
        data.put("fieldId", fieldId);
        data.put("gameSessionId", gameSessionId);
        data.put("userId", userId);
        data.put("siteId", siteId);
        data.put("siteName", siteName);
        data.put("timestamp", System.currentTimeMillis());

        WebSocketMessage message = new WebSocketMessage();
        message.setType("BOMB_DEFUSED");
        message.setSenderId(userId);
        message.setPayload(data);

        sendToGameSession(fieldId, message);
    }


    /**
     * Envoie une notification d'explosion de bombe
     */
    public void sendBombExplodedNotification(Long sessionId) {
        Map<String, Object> data = new HashMap<>();
        data.put("sessionId", sessionId);
        data.put("timestamp", System.currentTimeMillis());

        WebSocketMessage message = new WebSocketMessage();
        message.setType("BOMB_EXPLODED");
        message.setPayload(data);

        sendToGameSession(sessionId, message);
    }
}