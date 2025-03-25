package com.airsoft.gamemapmaster.scenario.treasurehunt.repository;

import com.airsoft.gamemapmaster.scenario.treasurehunt.model.TreasureHuntScenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TreasureHuntScenarioRepository extends JpaRepository<TreasureHuntScenario, Long> {
    Optional<TreasureHuntScenario> findByScenarioId(Long scenarioId);
}
