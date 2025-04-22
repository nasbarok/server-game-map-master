package com.airsoft.gamemapmaster.model.Data;

import lombok.Data;

@Data
public class TreasureFoundData {
    private String username;
    private String teamName;
    private int points;
    private int totalScore;
    private Boolean isNewLeader;
    private Long treasureId;
    private String treasureName;
    private String symbol;
    private Long gameSessionId;

    private Long scenarioId;
}