package com.airsoft.gamemapmaster.scenario.treasurehunt.service;

import com.airsoft.gamemapmaster.scenario.treasurehunt.model.TreasureHuntScore;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TreasureHuntScoreService {
    // Méthodes pour la gestion des scores
    TreasureHuntScore updateScore(Long treasureHuntScenarioId, Long userId, Integer pointsToAdd);
    TreasureHuntScore getOrCreateScore(Long treasureHuntScenarioId, Long userId, Long teamId);
    Optional<TreasureHuntScore> findScore(Long treasureHuntScenarioId, Long userId);

    // Méthodes pour récupérer les scores
    List<TreasureHuntScore> getIndividualScores(Long treasureHuntScenarioId);
    List<TreasureHuntScore> getTeamScores(Long treasureHuntScenarioId);
    Map<String, Object> getScoreboard(Long treasureHuntScenarioId);

    // Méthodes pour gérer l'état des scores
    void lockScores(Long treasureHuntScenarioId, Boolean locked);
    void resetScores(Long treasureHuntScenarioId);
    boolean areScoresLocked(Long treasureHuntScenarioId);
}
