package com.airsoft.gamemapmaster.scenario.bomboperation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class BombOperationSessionDto {
    private Long id;
    private BombOperationScenarioDto bombOperationScenario;
    private Long gameSessionId;
    private Integer currentRound;
    private Integer attackTeamScore;
    private Integer defenseTeamScore;
    private String gameState;
    private LocalDateTime roundStartTime;
    private LocalDateTime bombPlantedTime;
    private LocalDateTime defuseStartTime;
    private List<BombSiteDto> toActiveBombSites;
    private List<BombSiteDto> activeBombSites;
    private List<BombSiteDto> disableBombSites;

    private List<BombSiteDto> explodedBombSites;
    private Map<Long, String> teamRoles;
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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


    public BombOperationScenarioDto getBombOperationScenario() {
        return bombOperationScenario;
    }

    public void setBombOperationScenario(BombOperationScenarioDto bombOperationScenario) {
        this.bombOperationScenario = bombOperationScenario;
    }

    public Map<Long, String> getTeamRoles() {
        return teamRoles;
    }

    public void setTeamRoles(Map<Long, String> teamRoles) {
        this.teamRoles = teamRoles;
    }

    public List<BombSiteDto> getToActiveBombSites() {
        return toActiveBombSites;
    }

    public void setToActiveBombSites(List<BombSiteDto> toActiveBombSites) {
        this.toActiveBombSites = toActiveBombSites;
    }

    public List<BombSiteDto> getActiveBombSites() {
        return activeBombSites;
    }

    public void setActiveBombSites(List<BombSiteDto> activeBombSites) {
        this.activeBombSites = activeBombSites;
    }

    public List<BombSiteDto> getDisableBombSites() {
        return disableBombSites;
    }

    public void setDisableBombSites(List<BombSiteDto> disableBombSites) {
        this.disableBombSites = disableBombSites;
    }

    public List<BombSiteDto> getExplodedBombSites() {
        return explodedBombSites;
    }

    public void setExplodedBombSites(List<BombSiteDto> explodedBombSites) {
        this.explodedBombSites = explodedBombSites;
    }
}

