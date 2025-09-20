package com.airsoft.gamemapmaster.scenario.targetelimination.service;

import com.airsoft.gamemapmaster.scenario.targetelimination.model.Elimination;
import com.airsoft.gamemapmaster.scenario.targetelimination.model.PlayerTarget;
import com.airsoft.gamemapmaster.scenario.targetelimination.model.TargetEliminationScenario;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TargetEliminationService {

    // Gestion du scénario
    TargetEliminationScenario saveScenario(TargetEliminationScenario scenario);
    Optional<TargetEliminationScenario> findById(Long id);
    Optional<TargetEliminationScenario> findByScenarioId(Long scenarioId);

    // Gestion des cibles
    List<PlayerTarget> assignTargetsToPlayers(Long scenarioId, Long gameSessionId);
    Optional<PlayerTarget> findPlayerTarget(Long playerId, Long scenarioId);
    List<PlayerTarget> findTargetsByScenario(Long scenarioId);

    // Gestion des éliminations
    Optional<Elimination> recordElimination(String qrCode, Long killerId, Long gameSessionId);
    boolean isPlayerInCooldown(Long playerId, Long scenarioId);
    List<Elimination> findEliminationsByScenario(Long scenarioId, Long gameSessionId);

    // Génération QR
    List<Map<String, Object>> generateQRCodes(Long scenarioId);
    byte[] generateQRCodeImage(String qrCode, int width, int height) throws Exception;

    // Scores et classements
    Map<String, Object> getScoreboard(Long scenarioId, Long gameSessionId);
    boolean isNewLeaderAfterElimination(Long scenarioId, Long gameSessionId, Long playerId, int newScore);

    // Gestion d'état
    void activateScenario(Long scenarioId, boolean active);
    void lockScores(Long scenarioId, boolean locked);
    void resetScores(Long scenarioId, Long gameSessionId);
}