package com.airsoft.gamemapmaster.repository;

import com.airsoft.gamemapmaster.model.GameSession;
import com.airsoft.gamemapmaster.model.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    Optional<GameSession> findFirstByFieldIdAndActiveTrue(Long fieldId);

    List<GameSession> findByGameMapId(Long gameMapId);
    List<GameSession> findByActiveTrue();
    Optional<GameSession> findByIdAndActiveTrue(Long id);

    Optional<GameSession> findFirstByFieldIdAndActiveTrueOrderByStartTimeDesc(Long fieldId);

    List<GameSession> findByFieldId(Long fieldId);

    List<GameSession> findByParticipantsUserId(Long userId);
}
