package com.airsoft.gamemapmaster.scenario.bomboperation.repository;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombOperationScenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BombOperationScenarioRepository extends JpaRepository<BombOperationScenario, Long> {
    List<BombOperationScenario> findByScenarioId(Long scenarioId);
    Optional<BombOperationScenario> findByScenarioIdAndActiveTrue(Long scenarioId);
}
