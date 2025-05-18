package com.airsoft.gamemapmaster.scenario.bomboperation.dto;

public class BombOperationPlayerStateDto {
    private Long id;
    private Long sessionId;
    private Long userId;
    private String username;
    private String team;
    private Boolean isAlive;
    private Boolean hasDefuseKit;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public Boolean getIsAlive() {
        return isAlive;
    }

    public void setIsAlive(Boolean isAlive) {
        this.isAlive = isAlive;
    }

    public Boolean getHasDefuseKit() {
        return hasDefuseKit;
    }

    public void setHasDefuseKit(Boolean hasDefuseKit) {
        this.hasDefuseKit = hasDefuseKit;
    }
}
