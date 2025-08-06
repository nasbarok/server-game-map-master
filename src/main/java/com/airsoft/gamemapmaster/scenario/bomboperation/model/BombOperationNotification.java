package com.airsoft.gamemapmaster.scenario.bomboperation.model;


import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.websocket.WebSocketMessage;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class BombOperationNotification {
    // Type de notification (ROUND_START, BOMB_PLANTED, DEFUSE_START, etc.)
    private String type;

    // Message textuel (utilisé principalement pour les erreurs)
    private String message;

    // Identifiants
    private Long sessionId;
    private Long userId;
    private Long killerUserId;
    private Long siteId;

    // Informations de round
    private Integer roundNumber;
    private Integer attackTeamScore;
    private Integer defenseTeamScore;

    // Informations de bombe
    private String siteName;
    private Integer bombTimer;
    private Integer defuseTime;
    private Integer remainingTime;

    // Informations d'équipe
    private String winnerTeam;
    private String reason;

    // Informations de position
    private Double latitude;
    private Double longitude;
    private Boolean isInActiveSite;

    // Informations d'état
    private Boolean isAlive;
    private String gameState;

    // Horodatage
    private OffsetDateTime timestamp;

    // Liste d'identifiants de sites actifs
    private List<Long> activeBombSiteIds;

    // Données génériques (pour les notifications personnalisées)
    private Object data;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public Long getKillerUserId() {
        return killerUserId;
    }

    public void setKillerUserId(Long killerUserId) {
        this.killerUserId = killerUserId;
    }

    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    public Integer getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(Integer roundNumber) {
        this.roundNumber = roundNumber;
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

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
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

    public Integer getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(Integer remainingTime) {
        this.remainingTime = remainingTime;
    }

    public String getWinnerTeam() {
        return winnerTeam;
    }

    public void setWinnerTeam(String winnerTeam) {
        this.winnerTeam = winnerTeam;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Boolean getIsInActiveSite() {
        return isInActiveSite;
    }

    public void setIsInActiveSite(Boolean isInActiveSite) {
        this.isInActiveSite = isInActiveSite;
    }

    public Boolean getIsAlive() {
        return isAlive;
    }

    public void setIsAlive(Boolean isAlive) {
        this.isAlive = isAlive;
    }

    public String getGameState() {
        return gameState;
    }

    public void setGameState(String gameState) {
        this.gameState = gameState;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public List<Long> getActiveBombSiteIds() {
        return activeBombSiteIds;
    }

    public void setActiveBombSiteIds(List<Long> activeBombSiteIds) {
        this.activeBombSiteIds = activeBombSiteIds;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
    public static WebSocketMessage bombPlanted(BombOperationSession session, BombSite bombSite, User user, Long senderId) {
        BombOperationNotification notification = new BombOperationNotification();
        notification.setType("BOMB_PLANTED");
        notification.setMessage("Bombe posée sur le site " + bombSite.getName() + " par " + user.getUsername() + " !");

        BombPlantedData data = new BombPlantedData();
        data.setBombSiteId(bombSite.getId());
        data.setBombSiteName(bombSite.getName());
        data.setPlantedByUserId(user.getId());
        data.setPlantedByUsername(user.getUsername());
        data.setBombTimer(session.getBombOperationScenario().getBombTimer());
        data.setGameSessionId(session.getGameSessionId());
        data.setScenarioId(session.getBombOperationScenario().getScenario().getId());

        notification.setData(data);

        return new WebSocketMessage(
                notification.getType(),
                notification,
                senderId,
                System.currentTimeMillis()
        );
    }

    public static WebSocketMessage bombDefused(BombOperationSession session, User user, Long senderId) {
        BombOperationNotification notification = new BombOperationNotification();
        notification.setType("BOMB_DEFUSED");
        notification.setMessage("Bombe désamorcée par " + user.getUsername() + " !");

        BombDefusedData data = new BombDefusedData();
        data.setDefusedByUserId(user.getId());
        data.setDefusedByUsername(user.getUsername());
        data.setGameSessionId(session.getGameSessionId());
        data.setScenarioId(session.getBombOperationScenario().getScenario().getId());

        notification.setData(data);

        return new WebSocketMessage(
                notification.getType(),
                notification,
                senderId,
                System.currentTimeMillis()
        );
    }

    public static WebSocketMessage bombExploded(BombOperationSession session, Long senderId) {
        BombOperationNotification notification = new BombOperationNotification();
        notification.setType("BOMB_EXPLODED");
        notification.setMessage("La bombe a explosé !");

        BombExplodedData data = new BombExplodedData();
        data.setGameSessionId(session.getGameSessionId());
        data.setScenarioId(session.getBombOperationScenario().getScenario().getId());

        notification.setData(data);

        return new WebSocketMessage(
                notification.getType(),
                notification,
                senderId,
                System.currentTimeMillis()
        );
    }

    public static WebSocketMessage roundEnd(BombOperationSession session, String winnerTeam, String reason, Long senderId) {
        BombOperationNotification notification = new BombOperationNotification();
        notification.setType("BOMB_ROUND_END");
        notification.setMessage("Round terminé ! Victoire de l'équipe " + winnerTeam + " : " + reason);

        RoundEndData data = new RoundEndData();
        data.setRoundNumber(session.getCurrentRound());
        data.setWinnerTeam(winnerTeam);
        data.setReason(reason);
        data.setAttackScore(session.getAttackTeamScore());
        data.setDefenseScore(session.getDefenseTeamScore());
        data.setGameSessionId(session.getGameSessionId());
        data.setScenarioId(session.getBombOperationScenario().getScenario().getId());

        notification.setData(data);

        return new WebSocketMessage(
                notification.getType(),
                notification,
                senderId,
                System.currentTimeMillis()
        );
    }

    public static WebSocketMessage gameEnd(BombOperationSession session, String winnerTeam, Long senderId) {
        BombOperationNotification notification = new BombOperationNotification();
        notification.setType("BOMB_GAME_END");
        notification.setMessage("Partie terminée ! Victoire de l'équipe " + winnerTeam + " !");

        GameEndData data = new GameEndData();
        data.setWinnerTeam(winnerTeam);
        data.setFinalAttackScore(session.getAttackTeamScore());
        data.setFinalDefenseScore(session.getDefenseTeamScore());
        data.setTotalRounds(session.getCurrentRound());
        data.setGameSessionId(session.getGameSessionId());
        data.setScenarioId(session.getBombOperationScenario().getScenario().getId());

        notification.setData(data);

        return new WebSocketMessage(
                notification.getType(),
                notification,
                senderId,
                System.currentTimeMillis()
        );
    }

    // Classes internes pour les données des notifications
    @Data
    public static class RoundStartData {
        private Integer roundNumber;
        private Integer attackScore;
        private Integer defenseScore;
        private List<Long> activeBombSiteIds;
        private Integer roundDuration;
        private Long gameSessionId;
        private Long scenarioId;
    }

    @Data
    public static class BombPlantedData {
        private Long bombSiteId;
        private String bombSiteName;
        private Long plantedByUserId;
        private String plantedByUsername;
        private Integer bombTimer;
        private Long gameSessionId;
        private Long scenarioId;
    }

    @Data
    public static class BombDefusedData {
        private Long defusedByUserId;
        private String defusedByUsername;
        private Long gameSessionId;
        private Long scenarioId;
    }

    @Data
    public static class BombExplodedData {
        private Long gameSessionId;
        private Long scenarioId;
    }

    @Data
    public static class RoundEndData {
        private Integer roundNumber;
        private String winnerTeam;
        private String reason;
        private Integer attackScore;
        private Integer defenseScore;
        private Long gameSessionId;
        private Long scenarioId;
    }

    @Data
    public static class GameEndData {
        private String winnerTeam;
        private Integer finalAttackScore;
        private Integer finalDefenseScore;
        private Integer totalRounds;
        private Long gameSessionId;
        private Long scenarioId;
    }
}
