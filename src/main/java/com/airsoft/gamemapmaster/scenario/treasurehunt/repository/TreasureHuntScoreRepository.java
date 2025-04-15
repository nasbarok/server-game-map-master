package com.airsoft.gamemapmaster.scenario.treasurehunt.repository;

import com.airsoft.gamemapmaster.scenario.treasurehunt.model.TreasureHuntScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TreasureHuntScoreRepository extends JpaRepository<TreasureHuntScore, Long> {

    List<TreasureHuntScore> findByTreasureHuntScenarioId(Long treasureHuntScenarioId);

    List<TreasureHuntScore> findByTreasureHuntScenarioIdAndTeamId(Long treasureHuntScenarioId, Long teamId);

    List<TreasureHuntScore> findByGameSessionId(Long gameSessionId);

    Optional<TreasureHuntScore> findByTreasureHuntScenarioIdAndUserIdAndGameSessionId(Long treasureHuntScenarioId, Long userId, Long gameSessionId);

    @Query("SELECT s FROM TreasureHuntScore s WHERE s.treasureHuntScenario.id = :treasureHuntScenarioId AND s.gameSessionId = :gameSessionId ORDER BY s.score DESC")
    List<TreasureHuntScore> findTopScoresByTreasureHuntScenarioIdAndGameSessionId(Long treasureHuntScenarioId, Long gameSessionId);

    @Query("SELECT s FROM TreasureHuntScore s WHERE s.treasureHuntScenario.id = :treasureHuntScenarioId AND s.team.id = :teamId AND s.gameSessionId = :gameSessionId ORDER BY s.score DESC")
    List<TreasureHuntScore> findTopScoresByTreasureHuntScenarioIdAndTeamIdAndGameSessionId(Long treasureHuntScenarioId, Long teamId, Long gameSessionId);

    @Query("SELECT s FROM TreasureHuntScore s WHERE s.gameSessionId = :gameSessionId ORDER BY s.score DESC")
    List<TreasureHuntScore> findTopScoresByGameSessionId(Long gameSessionId);

    List<TreasureHuntScore> findByTreasureHuntScenarioIdAndGameSessionId(Long treasureHuntScenarioId, Long gameSessionId);
}
