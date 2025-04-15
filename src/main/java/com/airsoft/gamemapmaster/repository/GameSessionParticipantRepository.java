package com.airsoft.gamemapmaster.repository;

import com.airsoft.gamemapmaster.model.GameSessionParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameSessionParticipantRepository extends JpaRepository<GameSessionParticipant, Long> {

    List<GameSessionParticipant> findByGameSessionId(Long gameSessionId);

    List<GameSessionParticipant> findByUserId(Long userId);
}
