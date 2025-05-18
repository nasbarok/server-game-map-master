package com.airsoft.gamemapmaster.scenario.bomboperation.dto;

import java.time.LocalDateTime;
import java.util.List;

public class BombOperationSessionDto {
    private Long id;
    private Long bombOperationScenarioId;
    private Long gameSessionId;
    private Integer currentRound;
    private Integer attackTeamScore;
    private Integer defenseTeamScore;
    private String gameState;
    private LocalDateTime roundStartTime;
    private LocalDateTime bombPlantedTime;
    private LocalDateTime defuseStartTime;
    private List<Long> activeBombSiteIds;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBombOperationScenarioId() {
        return bombOperationScenarioId;
    }

    public void setBombOperationScenarioId(Long bombOperationScenarioId) {
        this.bombOperationScenarioId = bombOperationScenarioId;
    }

    public Long getGameSessionId() {
        return gameSessionId;
    }

    public void setGameSessionId(Long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public Integer getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(Integer currentRound) {
        this.currentRound = currentRound;
    }

    public Integer getAttackTeamScore() {
        return attackTeamScore;
    }

    public void setAttackTeamScore(Integer attackTeamScore) {
        this.attackTeamScore = attackTeamScore;
    }

    public Integer getDefenseTeamScore() {
        return defenseTeamScore;
    }

    public void setDefenseTeamScore(Integer defenseTeamScore) {
        this.defenseTeamScore = defenseTeamScore;
    }

    public String getGameState() {
        return gameState;
    }

    public void setGameState(String gameState) {
        this.gameState = gameState;
    }

    public LocalDateTime getRoundStartTime() {
        return roundStartTime;
    }

    public void setRoundStartTime(LocalDateTime roundStartTime) {
        this.roundStartTime = roundStartTime;
    }

    public LocalDateTime getBombPlantedTime() {
        return bombPlantedTime;
    }

    public void setBombPlantedTime(LocalDateTime bombPlantedTime) {
        this.bombPlantedTime = bombPlantedTime;
    }

    public LocalDateTime getDefuseStartTime() {
        return defuseStartTime;
    }

    public void setDefuseStartTime(LocalDateTime defuseStartTime) {
        this.defuseStartTime = defuseStartTime;
    }

    public List<Long> getActiveBombSiteIds() {
        return activeBombSiteIds;
    }

    public void setActiveBombSiteIds(List<Long> activeBombSiteIds) {
        this.activeBombSiteIds = activeBombSiteIds;
    }
}

