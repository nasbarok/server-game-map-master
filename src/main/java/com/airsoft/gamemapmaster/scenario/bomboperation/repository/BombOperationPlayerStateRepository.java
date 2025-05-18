package com.airsoft.gamemapmaster.scenario.bomboperation.repository;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombOperationPlayerState;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombOperationTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BombOperationPlayerStateRepository extends JpaRepository<BombOperationPlayerState, Long> {
    List<BombOperationPlayerState> findByBombOperationSessionId(Long bombOperationSessionId);
    Optional<BombOperationPlayerState> findByBombOperationSessionIdAndUserId(Long bombOperationSessionId, Long userId);

    List<BombOperationPlayerState> findByBombOperationSessionIdAndTeam(Long sessionId, BombOperationTeam team);
}
