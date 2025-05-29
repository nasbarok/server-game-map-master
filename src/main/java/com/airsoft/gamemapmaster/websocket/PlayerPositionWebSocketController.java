package com.airsoft.gamemapmaster.websocket;

import com.airsoft.gamemapmaster.model.DTO.PlayerPositionDTO;
import com.airsoft.gamemapmaster.service.PlayerPositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * Controller WebSocket pour la gestion des positions des joueurs en temps réel
 */
@Controller
public class PlayerPositionWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private PlayerPositionService playerPositionService;
    
    /**
     * Gère les mises à jour de position des joueurs
     * 
     * @param fieldId Identifiant du terrain
     * @param positionDTO DTO contenant les informations de position
     */
    @MessageMapping("/field/{fieldId}/positions")
    public void handlePlayerPosition(
            @DestinationVariable Long fieldId,
            PlayerPositionDTO positionDTO) {
        
        // Sauvegarder la position
        PlayerPositionDTO savedPosition = playerPositionService.savePosition(positionDTO);
        
        // Diffuser la position à tous les joueurs de la session
        messagingTemplate.convertAndSend(
            "/topic/field/" + fieldId + "/positions",
            savedPosition
        );
    }
}
