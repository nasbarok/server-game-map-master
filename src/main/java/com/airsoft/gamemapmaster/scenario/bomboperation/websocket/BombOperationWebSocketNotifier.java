package com.airsoft.gamemapmaster.scenario.bomboperation.websocket;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombOperationSession;
import com.airsoft.gamemapmaster.websocket.WebSocketMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class BombOperationWebSocketNotifier {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendToGameSession(Long gameSessionId, WebSocketMessage message) {
        messagingTemplate.convertAndSend("/topic/field/" + gameSessionId, message);
    }
}