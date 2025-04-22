package com.airsoft.gamemapmaster.repository;

import com.airsoft.gamemapmaster.model.GameSessionParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameSessionParticipantRepository extends JpaRepository<GameSessionParticipant, Long> {
    List<GameSessionParticipant> findByGameSessionId(Long gameSessionId);
    List<GameSessionParticipant> findByUserId(Long userId);
    Optional<GameSessionParticipant> findByGameSessionIdAndUserId(Long gameSessionId, Long userId);
    List<GameSessionParticipant> findByGameSessionIdAndLeftAtIsNull(Long gameSessionId);
    List<GameSessionParticipant> findByTeamId(Long teamId);
}
