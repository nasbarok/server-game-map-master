package com.airsoft.gamemapmaster.scenario.treasurehunt.model;

import com.airsoft.gamemapmaster.model.Data.TreasureFoundData;
import com.airsoft.gamemapmaster.websocket.WebSocketMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TreasureHuntNotification {

    private String type;
    private String message;
    private Object data;

    public static WebSocketMessage treasureFound(TreasureFound treasureFound,
                                                 String username,
                                                 String teamName,
                                                 int points,
                                                 int totalScore,
                                                 boolean isNewLeader,
                                                 Long senderId,
                                                 Long gameSessionId) {

        TreasureHuntNotification notification = new TreasureHuntNotification();
        notification.setType("TREASURE_FOUND");

        String message = username;
        if (teamName != null && !teamName.isEmpty()) {
            message += " de l'équipe " + teamName;
        }
        message += " a trouvé un trésor de " + points + " points!";
        if (isNewLeader) {
            message += " Ils prennent la tête avec " + totalScore + " points!";
        }

        notification.setMessage(message);

        TreasureFoundData data = new TreasureFoundData();
        data.setUsername(username);
        data.setTeamName(teamName);
        data.setPoints(points);
        data.setTotalScore(totalScore);
        data.setIsNewLeader(isNewLeader);
        data.setTreasureId(treasureFound.getTreasure().getId());
        data.setTreasureName(treasureFound.getTreasure().getName());
        data.setSymbol(treasureFound.getTreasure().getSymbol());
        data.setGameSessionId(gameSessionId);
        data.setScenarioId(treasureFound.getTreasure().getTreasureHuntScenario().getScenario().getId());

        notification.setData(data);

        return new WebSocketMessage(
                notification.getType(),
                notification,
                senderId,
                System.currentTimeMillis()
        );
    }


    public static TreasureHuntNotification scoreboardUpdate(Object scoreboard) {
        TreasureHuntNotification notification = new TreasureHuntNotification();
        notification.setType("SCOREBOARD_UPDATE");
        notification.setMessage("Mise à jour du tableau des scores");
        notification.setData(scoreboard);
        return notification;
    }
}
