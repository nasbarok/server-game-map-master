package com.airsoft.gamemapmaster.scenario.treasurehunt.repository;

import com.airsoft.gamemapmaster.scenario.treasurehunt.model.TreasureHuntScenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TreasureHuntScenarioRepository extends JpaRepository<TreasureHuntScenario, Long> {

    Optional<TreasureHuntScenario> findByScenarioId(Long scenarioId);

    @Query("SELECT ths FROM TreasureHuntScenario ths WHERE ths.scenario.id IN :scenarioIds")
    List<TreasureHuntScenario> findByScenarioIds(List<Long> scenarioIds);

    @Query("SELECT ths FROM TreasureHuntScenario ths WHERE ths.active = true")
    List<TreasureHuntScenario> findAllActive();
}
