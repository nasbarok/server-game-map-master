package com.airsoft.gamemapmaster.service;

import com.airsoft.gamemapmaster.model.Field;
import com.airsoft.gamemapmaster.model.GameSession;
import com.airsoft.gamemapmaster.model.Scenario;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface GameSessionService {
    Optional<GameSession> findActiveSessionByFieldId(Long fieldId);

    @Query("SELECT gs.id FROM GameSession gs WHERE gs.active = true")
    Long getCurrentGameSessionId();

    boolean isGameSessionActive(Long gameSessionId);

    GameSession startNewSession(Field field);

    Optional<GameSession> findById(Long sessionId);
}
