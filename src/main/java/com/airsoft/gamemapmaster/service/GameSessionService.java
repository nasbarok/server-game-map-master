package com.airsoft.gamemapmaster.service;

import com.airsoft.gamemapmaster.model.GameSession;

import java.util.Optional;

public interface GameSessionService {
    Optional<GameSession> findActiveSessionByFieldId(Long fieldId);
}
