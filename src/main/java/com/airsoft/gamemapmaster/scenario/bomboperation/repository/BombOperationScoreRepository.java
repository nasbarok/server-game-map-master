package com.airsoft.gamemapmaster.scenario.bomboperation.repository;

import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombOperationScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BombOperationScoreRepository extends JpaRepository<BombOperationScore, Long> {
    List<BombOperationScore> findByBombOperationScenarioIdAndGameSessionId(Long bombOperationScenarioId, Long gameSessionId);

    Optional<BombOperationScore> findByBombOperationScenarioIdAndUserIdAndGameSessionId(
            Long bombOperationScenarioId, Long userId, Long gameSessionId);

    @Query("SELECT s FROM BombOperationScore s WHERE s.bombOperationScenario.id = :scenarioId AND s.gameSessionId = :gameSessionId ORDER BY s.roundsWon DESC")
    List<BombOperationScore> findTopScoresByScenarioAndGameSession(
            @Param("scenarioId") Long scenarioId,
            @Param("gameSessionId") Long gameSessionId);

    @Query("SELECT s FROM BombOperationScore s WHERE s.bombOperationScenario.id = :scenarioId AND s.gameSessionId = :gameSessionId AND s.team.id = :teamId ORDER BY s.roundsWon DESC")
    List<BombOperationScore> findTopScoresByScenarioAndGameSessionAndTeam(
            @Param("scenarioId") Long scenarioId,
            @Param("gameSessionId") Long gameSessionId,
            @Param("teamId") Long teamId);
}
