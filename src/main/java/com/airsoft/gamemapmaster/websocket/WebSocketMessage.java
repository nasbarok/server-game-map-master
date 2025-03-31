package com.airsoft.gamemapmaster.websocket;

import com.airsoft.gamemapmaster.model.ConnectedPlayer;
import com.airsoft.gamemapmaster.model.Field;
import com.airsoft.gamemapmaster.model.Team;
import com.airsoft.gamemapmaster.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe générique pour les messages WebSocket
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {
    private String type;
    private Object payload;
    private String sender;
    private long timestamp;

    // Constructeurs, getters, setters...

    // Méthodes utilitaires pour créer des messages standardisés
    public static WebSocketMessage playerConnected(ConnectedPlayer player, Long fieldId) {
        Map<String, Object> playerData = new HashMap<>();
        playerData.put("id", player.getUser().getId());
        playerData.put("username", player.getUser().getUsername());
        if (player.getTeam() != null) {
            playerData.put("teamId", player.getTeam().getId());
            playerData.put("teamName", player.getTeam().getName());
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("player", playerData);
        payload.put("fieldId", fieldId);

        return new WebSocketMessage(
                "PLAYER_CONNECTED",
                payload,
                player.getUser().getUsername(),
                System.currentTimeMillis()
        );
    }

    public static WebSocketMessage playerDisconnected(User user, Long fieldId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", user.getId());
        payload.put("username", user.getUsername());
        payload.put("fieldId", fieldId);

        return new WebSocketMessage(
                "PLAYER_DISCONNECTED",
                payload,
                user.getUsername(),
                System.currentTimeMillis()
        );
    }

    public static WebSocketMessage teamUpdated(Team team) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("teamId", team.getId());
        payload.put("teamName", team.getName());
        payload.put("fieldId", team.getGameMap().getField().getId());

        return new WebSocketMessage(
                "TEAM_UPDATED",
                payload,
                "system",
                System.currentTimeMillis()
        );
    }

    public static WebSocketMessage fieldClosed(Field field) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("fieldId", field.getId());
        payload.put("fieldName", field.getName());

        return new WebSocketMessage(
                "FIELD_CLOSED",
                payload,
                "system",
                System.currentTimeMillis()
        );
    }
}