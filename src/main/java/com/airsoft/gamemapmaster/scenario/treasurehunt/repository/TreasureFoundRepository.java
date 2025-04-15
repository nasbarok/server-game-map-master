package com.airsoft.gamemapmaster.scenario.treasurehunt.repository;

import com.airsoft.gamemapmaster.scenario.treasurehunt.model.TreasureFound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TreasureFoundRepository extends JpaRepository<TreasureFound, Long> {

    List<TreasureFound> findByTreasureId(Long treasureId);

    List<TreasureFound> findByUserId(Long userId);

    List<TreasureFound> findByTeamId(Long teamId);

    List<TreasureFound> findByGameSessionId(Long gameSessionId);

    Optional<TreasureFound> findByTreasureIdAndUserIdAndGameSessionId(Long treasureId, Long userId, Long gameSessionId);

    @Query("SELECT tf FROM TreasureFound tf WHERE tf.treasure.treasureHuntScenario.id = :treasureHuntScenarioId AND tf.user.id = :userId AND tf.gameSessionId = :gameSessionId")
    List<TreasureFound> findByTreasureHuntScenarioIdAndUserIdAndGameSessionId(Long treasureHuntScenarioId, Long userId, Long gameSessionId);

    @Query("SELECT tf FROM TreasureFound tf WHERE tf.treasure.treasureHuntScenario.id = :treasureHuntScenarioId AND tf.team.id = :teamId AND tf.gameSessionId = :gameSessionId")
    List<TreasureFound> findByTreasureHuntScenarioIdAndTeamIdAndGameSessionId(Long treasureHuntScenarioId, Long teamId, Long gameSessionId);

    List<TreasureFound> findByTreasureIdAndGameSessionId(Long id, Long gameSessionId);
}
