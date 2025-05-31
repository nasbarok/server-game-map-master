package com.airsoft.gamemapmaster.websocket;

import com.airsoft.gamemapmaster.model.*;
import com.airsoft.gamemapmaster.model.DTO.GameSessionDTO;
import com.airsoft.gamemapmaster.model.DTO.GameSessionParticipantDTO;
import com.airsoft.gamemapmaster.model.DTO.GameSessionScenarioDTO;
import com.airsoft.gamemapmaster.position.dto.PlayerPositionDTO;
import com.airsoft.gamemapmaster.scenario.treasurehunt.model.Treasure;
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
    private Long senderId;
    private long timestamp;

    // Constructeurs, getters, setters...

    // Méthodes utilitaires pour créer des messages standardisés
    public static WebSocketMessage playerConnected(ConnectedPlayer player, Long fieldId, Long senderId) {
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
                senderId,
                System.currentTimeMillis()
        );
    }

    public static WebSocketMessage playerDisconnected(User user, Long fieldId, Long senderId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", user.getId());
        payload.put("username", user.getUsername());
        payload.put("fieldId", fieldId);

        return new WebSocketMessage(
                "PLAYER_DISCONNECTED",
                payload,
                senderId,
                System.currentTimeMillis()
        );
    }

    public static WebSocketMessage teamUpdated(Team team, Long senderId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("teamId", team.getId());
        payload.put("teamName", team.getName());
        payload.put("fieldId", team.getGameMap().getField().getId());

        return new WebSocketMessage(
                "TEAM_UPDATED",
                payload,
                senderId,
                System.currentTimeMillis()
        );
    }

    public static WebSocketMessage fieldClosed(Field field, Long senderId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("fieldId", field.getId());
        payload.put("fieldName", field.getName());

        return new WebSocketMessage(
                "FIELD_CLOSED",
                payload,
                senderId,
                System.currentTimeMillis()
        );
    }

    public static WebSocketMessage teamUpdateRemove(Long mapId, Long userId, Long fieldId, Long senderId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("mapId", mapId);
        payload.put("userId", userId);
        payload.put("teamId", null);
        payload.put("action", "REMOVE_FROM_TEAM");
        payload.put("fieldId", fieldId);

        return new WebSocketMessage(
                "TEAM_UPDATE",
                payload,
                senderId,
                System.currentTimeMillis()
        );
    }

    public static WebSocketMessage gameSessionStarted(GameSession session, Long senderId) {

        GameSessionDTO gameSessionDTO = GameSessionDTO.fromEntity(session);
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", gameSessionDTO.getId());
        payload.put("gameMap", gameSessionDTO.getGameMap()); // sérialisable avec @Json
        payload.put("field", gameSessionDTO.getField());
        payload.put("startTime", gameSessionDTO.getStartTime());
        payload.put("durationMinutes", gameSessionDTO.getDurationMinutes());
        payload.put("participants", gameSessionDTO.getParticipants()); // doit être sérialisable
        payload.put("scenarios", gameSessionDTO.getScenarios());       // idem

        return new WebSocketMessage(
                "GAME_SESSION_STARTED",
                payload,
                senderId,
                System.currentTimeMillis()
        );
    }

    public static WebSocketMessage gameSessionEnded(GameSession session, Long senderId) {
        GameSessionDTO gameSessionDTO = GameSessionDTO.fromEntity(session);
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", gameSessionDTO.getId());
        payload.put("endTime", gameSessionDTO.getEndTime());
        payload.put("field", gameSessionDTO.getField());
        payload.put("gameMap", gameSessionDTO.getGameMap());

        return new WebSocketMessage(
                "GAME_SESSION_ENDED",
                payload,
                senderId,
                System.currentTimeMillis()
        );
    }

    public static WebSocketMessage participantJoined(GameSessionParticipant participant, Long senderId) {
        GameSessionParticipantDTO participantDTO = GameSessionParticipantDTO.fromEntity(participant);
        Map<String, Object> payload = new HashMap<>();
        payload.put("gameSessionId", participant.getGameSession().getId());
        payload.put("participant", participantDTO);

        return new WebSocketMessage(
                "PARTICIPANT_JOINED",
                payload,
                senderId,
                System.currentTimeMillis()
        );
    }

    public static WebSocketMessage participantLeft(GameSessionParticipant participant, Long senderId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("gameSessionId", participant.getGameSession().getId());
        payload.put("userId", participant.getUser().getId());
        payload.put("username", participant.getUser().getUsername());

        return new WebSocketMessage(
                "PARTICIPANT_LEFT",
                payload,
                senderId,
                System.currentTimeMillis()
        );
    }


    public static WebSocketMessage scenarioAdded(GameSessionScenario scenario, Long gameSessionId, Long senderId) {
        GameSessionScenarioDTO scenarioDTO = GameSessionScenarioDTO.fromEntity(scenario);
        Map<String, Object> payload = new HashMap<>();
        payload.put("gameSessionId", gameSessionId);
        payload.put("scenario", scenarioDTO);

        return new WebSocketMessage(
                "SCENARIO_ADDED",
                payload,
                senderId,
                System.currentTimeMillis()
        );
    }

    public static WebSocketMessage scenarioActivated(Long gameSessionId, Long scenarioId, Long senderId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("gameSessionId", gameSessionId);
        payload.put("scenarioId", scenarioId);

        return new WebSocketMessage(
                "SCENARIO_ACTIVATED",
                payload,
                senderId,
                System.currentTimeMillis()
        );
    }

    public static WebSocketMessage scenarioDeactivated(Long gameSessionId, Long scenarioId, Long senderId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("gameSessionId", gameSessionId);
        payload.put("scenarioId", scenarioId);

        return new WebSocketMessage(
                "SCENARIO_DEACTIVATED",
                payload,
                senderId,
                System.currentTimeMillis()
        );
    }

    public static WebSocketMessage treasureFound(
            Treasure treasure,
            User user,
            Team team,
            Long gameSessionId,
            Integer currentScore,
            Long senderId
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("gameSessionId", gameSessionId);
        data.put("treasureId", treasure.getId());
        data.put("treasureName", treasure.getName());
        data.put("points", treasure.getPoints());
        data.put("symbol", treasure.getSymbol());
        data.put("currentScore", currentScore);
        data.put("teamId", team != null ? team.getId() : null);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "TREASURE_FOUND");
        payload.put("message", "Un trésor a été trouvé !");
        payload.put("data", data);

        return new WebSocketMessage(
                "TREASURE_FOUND",
                payload,
                senderId,
                System.currentTimeMillis()
        );
    }

    public static WebSocketMessage playerPosition(PlayerPositionDTO positionDTO, Long senderId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", positionDTO.getUserId());
        payload.put("gameSessionId", positionDTO.getGameSessionId());
        payload.put("teamId", positionDTO.getTeamId());
        payload.put("latitude", positionDTO.getLatitude());
        payload.put("longitude", positionDTO.getLongitude());
        payload.put("timestamp", positionDTO.getTimestamp());

        return new WebSocketMessage(
                "PLAYER_POSITION",
                payload,
                senderId,
                System.currentTimeMillis()
        );
    }


}