package com.airsoft.gamemapmaster.service;
import com.airsoft.gamemapmaster.model.Field;
import com.airsoft.gamemapmaster.model.GameSession;
import com.airsoft.gamemapmaster.model.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface HistoryService {

    List<Field> getFieldsByOwnerId(Long ownerId);

    Optional<Field> getFieldById(Long id);

    List<GameSession> getGameSessionsByFieldId(Long fieldId);

    List<GameSession> getGameSessionsByParticipantId(Long userId);

    Optional<GameSession> getGameSessionById(Long id);

    void deleteGameSession(Long id);

    Map<String, Object> getGameSessionStatistics(Long gameSessionId);

    boolean isUserAuthorizedForGameSession(User user, GameSession gameSession);

    void deleteFieldAndHistory(Long id);
}
