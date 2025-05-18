package com.airsoft.gamemapmaster.scenario.bomboperation.repository;

import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombOperationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BombOperationSessionRepository extends JpaRepository<BombOperationSession, Long> {
    Optional<BombOperationSession> findByGameSessionId(Long gameSessionId);
}
