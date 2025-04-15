package com.airsoft.gamemapmaster.controller;

import com.airsoft.gamemapmaster.model.GameSessionParticipant;
import com.airsoft.gamemapmaster.repository.GameSessionParticipantRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/participants")
public class GameSessionParticipantController {

    private final GameSessionParticipantRepository participantRepository;

    public GameSessionParticipantController(GameSessionParticipantRepository participantRepository) {
        this.participantRepository = participantRepository;
    }

    // 1. Tous les participants d'une game_session
    @GetMapping("/session/{gameSessionId}")
    public List<GameSessionParticipant> getParticipantsBySession(@PathVariable Long gameSessionId) {
        return participantRepository.findByGameSessionId(gameSessionId);
    }

    // 2. Tout l'historique d'un utilisateur
    @GetMapping("/user/{userId}")
    public List<GameSessionParticipant> getAllParticipationByUser(@PathVariable Long userId) {
        return participantRepository.findByUserId(userId);
    }

    // 3. Historique d'un utilisateur sur un terrain précis
    @GetMapping("/user/{userId}/field/{fieldId}")
    public List<GameSessionParticipant> getParticipationByUserAndField(@PathVariable Long userId, @PathVariable Long fieldId) {
        List<GameSessionParticipant> all = participantRepository.findByUserId(userId);
        return all.stream()
                .filter(p -> p.getGameSession().getField().getId().equals(fieldId))
                .collect(Collectors.toList());
    }
}
