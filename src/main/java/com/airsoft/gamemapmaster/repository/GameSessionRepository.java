package com.airsoft.gamemapmaster.repository;

import com.airsoft.gamemapmaster.model.GameSession;
import com.airsoft.gamemapmaster.model.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    Optional<GameSession> findFirstByFieldIdAndActiveTrue(Long fieldId);
}
