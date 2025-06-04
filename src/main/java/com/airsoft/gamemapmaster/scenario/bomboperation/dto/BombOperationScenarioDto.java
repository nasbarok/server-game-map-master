package com.airsoft.gamemapmaster.scenario.bomboperation.dto;

import java.util.List;
import java.util.stream.Collectors;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.*;

public class BombOperationScenarioDto {
    private Long id;
    private Long scenarioId;
    private Integer bombTimer;
    private Integer defuseTime;
    private Integer activeSites;
    private String attackTeamName;
    private String defenseTeamName;
    private Boolean active;
    private Boolean showZones;
    private Boolean showPointsOfInterest;

    private List<BombSiteDto> bombSites;



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

    public Boolean getShowZones() {
        return showZones;
    }

    public void setShowZones(Boolean showZones) {
        this.showZones = showZones;
    }

    public Boolean getShowPointsOfInterest() {
        return showPointsOfInterest;
    }

    public void setShowPointsOfInterest(Boolean showPointsOfInterest) {
        this.showPointsOfInterest = showPointsOfInterest;
    }

    public List<BombSiteDto> getBombSites() {
        return bombSites;
    }

    public void setBombSites(List<BombSiteDto> bombSites) {
        this.bombSites = bombSites;
    }
}
