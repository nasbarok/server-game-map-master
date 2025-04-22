package com.airsoft.gamemapmaster.repository;

import com.airsoft.gamemapmaster.model.GameSessionScenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameSessionScenarioRepository extends JpaRepository<GameSessionScenario, Long> {
    List<GameSessionScenario> findByGameSessionId(Long gameSessionId);
    List<GameSessionScenario> findByScenarioId(Long scenarioId);
    Optional<GameSessionScenario> findByGameSessionIdAndScenarioId(Long gameSessionId, Long scenarioId);
    List<GameSessionScenario> findByGameSessionIdAndActiveTrue(Long gameSessionId);
    List<GameSessionScenario> findByGameSessionIdAndIsMainScenarioTrue(Long gameSessionId);
}
