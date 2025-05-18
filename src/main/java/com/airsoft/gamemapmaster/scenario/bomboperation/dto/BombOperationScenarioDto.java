package com.airsoft.gamemapmaster.scenario.bomboperation.dto;
public class BombOperationScenarioDto {
    private Long id;
    private Long scenarioId;
    private Integer roundDuration;
    private Integer bombTimer;
    private Integer defuseTime;
    private Integer maxRounds;
    private Integer activeSites;
    private String attackTeamName;
    private String defenseTeamName;
    private Boolean active;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getScenarioId() {
        return scenarioId;
    }

    public void setScenarioId(Long scenarioId) {
        this.scenarioId = scenarioId;
    }

    public Integer getRoundDuration() {
        return roundDuration;
    }

    public void setRoundDuration(Integer roundDuration) {
        this.roundDuration = roundDuration;
    }

    public Integer getBombTimer() {
        return bombTimer;
    }

    public void setBombTimer(Integer bombTimer) {
        this.bombTimer = bombTimer;
    }

    public Integer getDefuseTime() {
        return defuseTime;
    }

    public void setDefuseTime(Integer defuseTime) {
        this.defuseTime = defuseTime;
    }

    public Integer getMaxRounds() {
        return maxRounds;
    }

    public void setMaxRounds(Integer maxRounds) {
        this.maxRounds = maxRounds;
    }

    public Integer getActiveSites() {
        return activeSites;
    }

    public void setActiveSites(Integer activeSites) {
        this.activeSites = activeSites;
    }

    public String getAttackTeamName() {
        return attackTeamName;
    }

    public void setAttackTeamName(String attackTeamName) {
        this.attackTeamName = attackTeamName;
    }

    public String getDefenseTeamName() {
        return defenseTeamName;
    }

    public void setDefenseTeamName(String defenseTeamName) {
        this.defenseTeamName = defenseTeamName;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
