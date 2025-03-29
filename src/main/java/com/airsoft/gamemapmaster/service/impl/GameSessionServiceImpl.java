package com.airsoft.gamemapmaster.service.impl;

import com.airsoft.gamemapmaster.model.GameSession;
import com.airsoft.gamemapmaster.repository.GameSessionRepository;
import com.airsoft.gamemapmaster.service.GameSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GameSessionServiceImpl implements GameSessionService {

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Override
    public Optional<GameSession> findActiveSessionByFieldId(Long fieldId) {
        return gameSessionRepository.findFirstByFieldIdAndActiveTrue(fieldId);
    }
}
