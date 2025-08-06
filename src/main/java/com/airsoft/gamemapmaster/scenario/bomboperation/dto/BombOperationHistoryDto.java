package com.airsoft.gamemapmaster.scenario.bomboperation.dto;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * DTO pour l'historique complet d'une session Bomb Operation
 * Contient toutes les données nécessaires pour le replay
 */
@Data
public class BombOperationHistoryDto {
    
    private Long gameSessionId;
    private OffsetDateTime sessionStartTime;
    private OffsetDateTime sessionEndTime;
    private String sessionStatus;
    
    // Informations sur le scénario
    private String scenarioName;
    private Integer bombTimer;
    private Integer defuseTime;
    private Integer armingTime;
    private Integer activeSites;
    
    // Informations sur les équipes
    private TeamHistoryDto attackTeam;
    private TeamHistoryDto defenseTeam;
    
    // Historique complet des sites
    private List<BombSiteHistoryDto> bombSitesHistory;
    
    // Timeline des événements
    private List<BombEventDto> timeline;
    
    // Statistiques finales
    private BombOperationStatsDto finalStats;
    
    /**
     * DTO pour l'historique d'une équipe
     */
    @Data
    public static class TeamHistoryDto {
        private String teamName;
        private String role; // "ATTACK" ou "DEFENSE"
        private List<String> playerNames;
        private Integer finalScore;
        private Boolean hasWon;
    }
    
    /**
     * DTO pour un événement dans la timeline
     */
    @Data
    public static class BombEventDto {
        private OffsetDateTime timestamp;
        private String eventType; // "ACTIVATED", "ARMED", "DISARMED", "EXPLODED"
        private String siteName;
        private String playerName;
        private String teamRole;
        private String description;
        private Integer timeRemainingSeconds; // Pour les événements d'armement

        private Long userId; // ID du joueur associé à l'événement
        public OffsetDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(OffsetDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public String getEventType() {
            return eventType;
        }

        public void setEventType(String eventType) {
            this.eventType = eventType;
        }

        public String getSiteName() {
            return siteName;
        }

        public void setSiteName(String siteName) {
            this.siteName = siteName;
        }

        public String getPlayerName() {
            return playerName;
        }

        public void setPlayerName(String playerName) {
            this.playerName = playerName;
        }

        public String getTeamRole() {
            return teamRole;
        }

        public void setTeamRole(String teamRole) {
            this.teamRole = teamRole;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Integer getTimeRemainingSeconds() {
            return timeRemainingSeconds;
        }

        public void setTimeRemainingSeconds(Integer timeRemainingSeconds) {
            this.timeRemainingSeconds = timeRemainingSeconds;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }
    }
    
    /**
     * DTO pour les statistiques finales
     */
    @Data
    public static class BombOperationStatsDto {
        private Integer totalSites;
        private Integer activatedSites;
        private Integer armedSites;
        private Integer disarmedSites;
        private Integer explodedSites;
        private String winningTeam;
        private String winCondition; // "MORE_EXPLOSIONS", "MORE_DISARMS", "TIME_EXPIRED"
        private Long sessionDurationMinutes;

        private Long gameSessionId; // Pour lier les stats à la session
        private List<BombOperationHistoryDto.BombEventDto> timeline; // Timeline des événements pour les stats
        private String result;

        public Integer getTotalSites() {
            return totalSites;
        }

        public void setTotalSites(Integer totalSites) {
            this.totalSites = totalSites;
        }

        public Integer getActivatedSites() {
            return activatedSites;
        }

        public void setActivatedSites(Integer activatedSites) {
            this.activatedSites = activatedSites;
        }

        public Integer getArmedSites() {
            return armedSites;
        }

        public void setArmedSites(Integer armedSites) {
            this.armedSites = armedSites;
        }

        public Integer getDisarmedSites() {
            return disarmedSites;
        }

        public void setDisarmedSites(Integer disarmedSites) {
            this.disarmedSites = disarmedSites;
        }

        public Integer getExplodedSites() {
            return explodedSites;
        }

        public void setExplodedSites(Integer explodedSites) {
            this.explodedSites = explodedSites;
        }

        public String getWinningTeam() {
            return winningTeam;
        }

        public void setWinningTeam(String winningTeam) {
            this.winningTeam = winningTeam;
        }

        public String getWinCondition() {
            return winCondition;
        }

        public void setWinCondition(String winCondition) {
            this.winCondition = winCondition;
        }

        public Long getSessionDurationMinutes() {
            return sessionDurationMinutes;
        }

        public void setSessionDurationMinutes(Long sessionDurationMinutes) {
            this.sessionDurationMinutes = sessionDurationMinutes;
        }

        public Long getGameSessionId() {
            return gameSessionId;
        }

        public void setGameSessionId(Long gameSessionId) {
            this.gameSessionId = gameSessionId;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public List<BombEventDto> getTimeline() {
            return timeline;
        }

        public void setTimeline(List<BombEventDto> timeline) {
            this.timeline = timeline;
        }
    }

    public Long getGameSessionId() {
        return gameSessionId;
    }

    public void setGameSessionId(Long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public OffsetDateTime getSessionStartTime() {
        return sessionStartTime;
    }

    public void setSessionStartTime(OffsetDateTime sessionStartTime) {
        this.sessionStartTime = sessionStartTime;
    }

    public OffsetDateTime getSessionEndTime() {
        return sessionEndTime;
    }

    public void setSessionEndTime(OffsetDateTime sessionEndTime) {
        this.sessionEndTime = sessionEndTime;
    }

    public String getSessionStatus() {
        return sessionStatus;
    }

    public void setSessionStatus(String sessionStatus) {
        this.sessionStatus = sessionStatus;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
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

    public Integer getArmingTime() {
        return armingTime;
    }

    public void setArmingTime(Integer armingTime) {
        this.armingTime = armingTime;
    }

    public Integer getActiveSites() {
        return activeSites;
    }

    public void setActiveSites(Integer activeSites) {
        this.activeSites = activeSites;
    }

    public TeamHistoryDto getAttackTeam() {
        return attackTeam;
    }

    public void setAttackTeam(TeamHistoryDto attackTeam) {
        this.attackTeam = attackTeam;
    }

    public TeamHistoryDto getDefenseTeam() {
        return defenseTeam;
    }

    public void setDefenseTeam(TeamHistoryDto defenseTeam) {
        this.defenseTeam = defenseTeam;
    }

    public List<BombSiteHistoryDto> getBombSitesHistory() {
        return bombSitesHistory;
    }

    public void setBombSitesHistory(List<BombSiteHistoryDto> bombSitesHistory) {
        this.bombSitesHistory = bombSitesHistory;
    }

    public List<BombEventDto> getTimeline() {
        return timeline;
    }

    public void setTimeline(List<BombEventDto> timeline) {
        this.timeline = timeline;
    }

    public BombOperationStatsDto getFinalStats() {
        return finalStats;
    }

    public void setFinalStats(BombOperationStatsDto finalStats) {
        this.finalStats = finalStats;
    }
}

