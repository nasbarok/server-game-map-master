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
    Optional<TreasureHuntScore> findByTreasureHuntScenarioIdAndUserId(Long treasureHuntScenarioId, Long userId);

    @Query("SELECT ths FROM TreasureHuntScore ths WHERE ths.treasureHuntScenario.id = ?1 ORDER BY ths.score DESC")
    List<TreasureHuntScore> findByTreasureHuntScenarioIdOrderByScoreDesc(Long treasureHuntScenarioId);

    @Query("SELECT ths FROM TreasureHuntScore ths WHERE ths.treasureHuntScenario.id = ?1 AND ths.team.id IS NOT NULL GROUP BY ths.team.id ORDER BY SUM(ths.score) DESC")
    List<Object[]> findTeamScoresByTreasureHuntScenarioIdOrderByScoreDesc(Long treasureHuntScenarioId);

    void deleteByTreasureHuntScenarioId(Long treasureHuntScenarioId);

    List<TreasureHuntScore> findByGameSessionId(Long gameSessionId);

    Optional<TreasureHuntScore> findByTreasureHuntScenarioIdAndUserIdAndGameSessionId(Long treasureHuntScenarioId, Long userId, Long gameSessionId);

    @Query("SELECT s FROM TreasureHuntScore s WHERE s.treasureHuntScenario.id = :treasureHuntScenarioId AND s.gameSessionId = :gameSessionId ORDER BY s.score DESC")
    List<TreasureHuntScore> findTopScoresByTreasureHuntScenarioIdAndGameSessionId(Long treasureHuntScenarioId, Long gameSessionId);

    @Query("SELECT s FROM TreasureHuntScore s WHERE s.treasureHuntScenario.id = :treasureHuntScenarioId AND s.team.id = :teamId AND s.gameSessionId = :gameSessionId ORDER BY s.score DESC")
    List<TreasureHuntScore> findTopScoresByTreasureHuntScenarioIdAndTeamIdAndGameSessionId(Long treasureHuntScenarioId, Long teamId, Long gameSessionId);

    @Query("SELECT s FROM TreasureHuntScore s WHERE s.gameSessionId = :gameSessionId ORDER BY s.score DESC")
    List<TreasureHuntScore> findTopScoresByGameSessionId(Long gameSessionId);
    @Query("SELECT s FROM TreasureHuntScore s WHERE s.treasureHuntScenario.id = :treasureHuntScenarioId AND s.gameSessionId = :gameSessionId ORDER BY s.score DESC")
    List<TreasureHuntScore> findByTreasureHuntScenarioIdAndGameSessionId(Long treasureHuntScenarioId, Long gameSessionId);
}
