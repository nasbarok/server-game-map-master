package com.airsoft.gamemapmaster.scenario.treasurehunt.service;

import com.airsoft.gamemapmaster.scenario.treasurehunt.model.Treasure;
import com.airsoft.gamemapmaster.scenario.treasurehunt.model.TreasureFound;
import com.airsoft.gamemapmaster.scenario.treasurehunt.model.TreasureHuntScenario;

import java.util.List;
import java.util.Optional;

public interface TreasureHuntService {
    // Méthodes pour TreasureHuntScenario
    TreasureHuntScenario saveTreasureHuntScenario(TreasureHuntScenario treasureHuntScenario);
    Optional<TreasureHuntScenario> findById(Long id);
    Optional<TreasureHuntScenario> findByScenarioId(Long scenarioId);
    
    // Méthodes pour Treasure
    Treasure saveTreasure(Treasure treasure);
    Optional<Treasure> findTreasureById(Long id);
    List<Treasure> findTreasuresByTreasureHuntId(Long treasureHuntId);
    Optional<Treasure> findTreasureByQrCode(String qrCode);
    
    // Méthodes pour TreasureFound
    TreasureFound saveTreasureFound(TreasureFound treasureFound);
    List<TreasureFound> findTreasuresFoundByUserId(Long userId);
    List<TreasureFound> findTreasuresFoundByTeamId(Long teamId);
    List<TreasureFound> findTreasuresFoundByTreasureId(Long treasureId);
}
