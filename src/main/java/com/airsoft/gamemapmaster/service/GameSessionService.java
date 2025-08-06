package com.airsoft.gamemapmaster.service;

import com.airsoft.gamemapmaster.model.*;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.List;

public interface GameSessionService {

    // Méthodes pour GameSession
    GameSession createGameSession(GameSession gameSession);
    GameSession startGameSession(Long gameSessionId, OffsetDateTime startTime);
    GameSession endGameSession(Long gameSessionId,OffsetDateTime endTime);
    Optional<GameSession> findById(Long gameSessionId);
    Optional<GameSession> findActiveGameSession(Long gameSessionId);
    List<GameSession> findAllActiveGameSessions();
    List<GameSession> findByGameMapId(Long gameMapId);

    // Méthodes pour GameSessionParticipant
    GameSessionParticipant addParticipant(Long gameSessionId, Long userId, Long teamId, Boolean isHost);
    GameSessionParticipant removeParticipant(Long gameSessionId, Long userId);
    List<GameSessionParticipant> getParticipants(Long gameSessionId);
    List<GameSessionParticipant> getActiveParticipants(Long gameSessionId);
    Optional<GameSessionParticipant> findParticipant(Long gameSessionId, Long userId);

    // Méthodes pour GameSessionScenario
    GameSessionScenario addScenario(Long gameSessionId, Long scenarioId, Boolean isMainScenario);
    GameSessionScenario activateScenario(Long gameSessionId, Long scenarioId);
    GameSessionScenario deactivateScenario(Long gameSessionId, Long scenarioId);
    List<GameSessionScenario> getScenarios(Long gameSessionId);
    List<GameSessionScenario> getActiveScenarios(Long gameSessionId);
    Optional<GameSessionScenario> findScenario(Long gameSessionId, Long scenarioId);

    // Méthodes utilitaires
    long getRemainingTimeInSeconds(Long gameSessionId);
    boolean isGameSessionActive(Long gameSessionId);


    void checkAndEndExpiredGameSessions();

    Optional<GameSession> findActiveSessionByFieldId(Long fieldId);
    @Query("SELECT gs.id FROM GameSession gs WHERE gs.active = true")
    Long getCurrentGameSessionId();

    GameSession startNewSession(Field field);

}
