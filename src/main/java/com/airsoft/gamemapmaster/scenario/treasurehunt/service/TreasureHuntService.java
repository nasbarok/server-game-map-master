package com.airsoft.gamemapmaster.scenario.treasurehunt.service;

import com.airsoft.gamemapmaster.model.Team;
import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.scenario.treasurehunt.model.Treasure;
import com.airsoft.gamemapmaster.scenario.treasurehunt.model.TreasureFound;
import com.airsoft.gamemapmaster.scenario.treasurehunt.model.TreasureHuntScenario;
import com.airsoft.gamemapmaster.scenario.treasurehunt.model.TreasureHuntScore;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TreasureHuntService {
    // Méthodes pour TreasureHuntScenario
    TreasureHuntScenario saveTreasureHuntScenario(TreasureHuntScenario treasureHuntScenario);
    Optional<TreasureHuntScenario> findById(Long id);
    Optional<TreasureHuntScenario> findByScenarioId(Long scenarioId);
    void lockScores(Long treasureHuntScenarioId, boolean locked);
    void resetScores(Long treasureHuntScenarioId, Long gameSessionId);
    void activateScenario(Long treasureHuntScenarioId, boolean active);
    List<TreasureHuntScenario> findActiveScenarios();

    // Méthodes pour Treasure
    Treasure saveTreasure(Treasure treasure);
    Optional<Treasure> findTreasureById(Long id);
    List<Treasure> findTreasuresByTreasureHuntId(Long treasureHuntId);
    Optional<Treasure> findTreasureByQrCode(String qrCode);
    List<Treasure> createTreasuresBatch(Long treasureHuntId, int count, int defaultValue, String defaultSymbol);
    Treasure updateTreasure(Long treasureId, String name, Integer points, String symbol);

    // Méthodes pour TreasureFound
    TreasureFound saveTreasureFound(TreasureFound treasureFound);
    List<TreasureFound> findTreasuresFoundByUserId(Long userId);
    List<TreasureFound> findTreasuresFoundByTeamId(Long teamId);
    List<TreasureFound> findTreasuresFoundByGameSessionId(Long gameSessionId);
    boolean recordTreasureFound(String qrCode, Long userId, Long teamId, Long gameSessionId);

    // Méthodes pour TreasureHuntScore
    TreasureHuntScore getOrCreateScore(Long treasureHuntScenarioId, User user, Team team, Long gameSessionId);
    List<TreasureHuntScore> getScoresByTreasureHuntScenarioIdAndGameSessionId(Long treasureHuntScenarioId, Long gameSessionId);
    List<TreasureHuntScore> getScoresByGameSessionId(Long gameSessionId);
    Map<String, Object> getScoreboard(Long treasureHuntScenarioId, Long gameSessionId);

    // Méthodes pour QR Codes
    List<Map<String, Object>> generateQRCodes(Long treasureHuntScenarioId);
    String generateQRCodeContent(Treasure treasure);
    byte[] generateQRCodeImage(String content, int width, int height);

    // Méthodes pour la gestion de partie
    void handleGameStart(Long scenarioId, Long gameSessionId);
    void handleGameEnd(Long scenarioId, Long gameSessionId);

    void deleteTreasureHuntScenarioById(Long id);

    void deleteTreasure(Treasure treasure);
}
