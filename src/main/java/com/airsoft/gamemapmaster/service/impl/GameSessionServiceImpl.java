package com.airsoft.gamemapmaster.service.impl;

import com.airsoft.gamemapmaster.model.Field;
import com.airsoft.gamemapmaster.model.GameSession;
import com.airsoft.gamemapmaster.model.Scenario;
import com.airsoft.gamemapmaster.repository.GameSessionRepository;
import com.airsoft.gamemapmaster.service.GameSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class GameSessionServiceImpl implements GameSessionService {

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Override
    public Optional<GameSession> findActiveSessionByFieldId(Long fieldId) {
        return gameSessionRepository.findFirstByFieldIdAndActiveTrue(fieldId);
    }

    @Override
    public Long getCurrentGameSessionId() {
        return null;
    }

    @Override
    public boolean isGameSessionActive(Long gameSessionId) {
        return false;
    }

    @Override
    public GameSession startNewSession(Field field) {
        GameSession session = new GameSession();
        session.setField(field);
        session.setActive(true);
        session.setStatus("RUNNING");
        session.setStartTime(LocalDateTime.now());
        return gameSessionRepository.save(session);
    }

    @Override
    public Optional<GameSession> findById(Long sessionId) {
        return gameSessionRepository.findById(sessionId);
    }
}
