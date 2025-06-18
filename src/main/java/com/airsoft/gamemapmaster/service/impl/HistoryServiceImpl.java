package com.airsoft.gamemapmaster.service.impl;

import com.airsoft.gamemapmaster.model.*;
import com.airsoft.gamemapmaster.position.repository.PlayerPositionRepository;
import com.airsoft.gamemapmaster.repository.*;
import com.airsoft.gamemapmaster.scenario.treasurehunt.repository.TreasureHuntScoreRepository;
import com.airsoft.gamemapmaster.scenario.treasurehunt.service.TreasureHuntService;
import com.airsoft.gamemapmaster.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.*;

@Service
public class HistoryServiceImpl implements HistoryService {
    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private GameSessionScenarioRepository gameSessionScenarioRepository;

    @Autowired
    private TreasureHuntService treasureHuntService;

    @Autowired
    private TreasureHuntScoreRepository treasureHuntScoreRepository;
    @Autowired
    private GameSessionParticipantRepository participantRepository;

    @Autowired
    private GameMapRepository gameMapRepository;

    @Autowired
    private FieldScenarioRepository fieldScenarioRepository;

    @Autowired
    private FieldUserHistoryRepository fieldUserHistoryRepository;

    @Autowired
    private PlayerPositionRepository playerPositionRepository;
    /**
     * Récupère tous les terrains pour un utilisateur donné
     */
    public List<Field> getFieldsByOwnerId(Long ownerId) {
        return fieldRepository.findByOwnerId(ownerId);
    }

    /**
     * Récupère un terrain par son ID
     */
    public Optional<Field> getFieldById(Long id) {
        return fieldRepository.findById(id);
    }

    /**
     * Récupère toutes les sessions de jeu pour un terrain donné
     */
    public List<GameSession> getGameSessionsByFieldId(Long fieldId) {
        return gameSessionRepository.findByFieldId(fieldId);
    }

    /**
     * Récupère toutes les sessions de jeu auxquelles un utilisateur a participé
     */
    public List<GameSession> getGameSessionsByParticipantId(Long userId) {
        return gameSessionRepository.findByParticipantsUserId(userId);
    }

    /**
     * Récupère une session de jeu par son ID
     */
    public Optional<GameSession> getGameSessionById(Long id) {
        return gameSessionRepository.findById(id);
    }

    /**
     * Supprime une session de jeu
     */
    @Override
    @Transactional
    public void deleteGameSession(Long id) {
        if (!gameSessionRepository.existsById(id)) {
            throw new EntityNotFoundException("Session de jeu non trouvée pour l'id : " + id);
        }

        // Supprime d'abord les positions liées
        playerPositionRepository.deleteByGameSessionId(id);

        // Puis la session
        gameSessionRepository.deleteById(id);
    }

    /**
     * Récupère les statistiques d'une session de jeu
     */
    public Map<String, Object> getGameSessionStatistics(Long gameSessionId) {
        Optional<GameSession> gameSessionOpt = gameSessionRepository.findById(gameSessionId);
        if (gameSessionOpt.isEmpty()) {
            return Collections.emptyMap();
        }

        GameSession gameSession = gameSessionOpt.get();
        Map<String, Object> statistics = new HashMap<>();

        // Informations de base sur la session
        statistics.put("id", gameSession.getId());
        statistics.put("startTime", gameSession.getStartTime());
        statistics.put("endTime", gameSession.getEndTime());
        statistics.put("active", gameSession.getActive());

        // Participants
        statistics.put("totalParticipants", gameSession.getParticipants().size());

        // Statistiques par scénario
        List<GameSessionScenario> scenarios = gameSessionScenarioRepository.findByGameSessionId(gameSessionId);
        List<Map<String, Object>> scenarioStats = new ArrayList<>();

        for (GameSessionScenario gameSessionScenario : scenarios) {
            Map<String, Object> scenarioStat = new HashMap<>();
            scenarioStat.put("scenarioId", gameSessionScenario.getScenario().getId());
            scenarioStat.put("scenarioName", gameSessionScenario.getScenario().getName());

            // Si c'est un scénario de chasse au trésor, ajouter les statistiques spécifiques
            if (gameSessionScenario.getScenario().getType().equals("treasure_hunt")) {
                // Récupérer les scores de la chasse au trésor
                Map<String, Object> treasureHuntStats = treasureHuntService.getScoreboardData(gameSessionId, gameSessionScenario.getScenario().getId());
                scenarioStat.put("treasureHuntStats", treasureHuntStats);
            }

            scenarioStats.add(scenarioStat);
        }

        statistics.put("scenarios", scenarioStats);
        return statistics;
    }

    /**
     * Vérifie si un utilisateur est autorisé à accéder à une session de jeu
     */
    public boolean isUserAuthorizedForGameSession(User user, GameSession gameSession) {
        // L'utilisateur est autorisé s'il est le propriétaire du terrain ou s'il a participé à la session
        return gameSession.getField().getOwner().getId().equals(user.getId()) ||
                gameSession.getParticipants().stream().anyMatch(p -> p.getUser().getId().equals(user.getId()));
    }

    @Override
    @Transactional
    public void deleteFieldAndHistory(Long fieldId) {
        // 1. Détacher les GameMaps associés au terrain (on ne les supprime pas)
        List<GameMap> maps = gameMapRepository.findByFieldId(fieldId);
        for (GameMap map : maps) {
            map.setField(null);
        }
        gameMapRepository.saveAll(maps);

        // 2. Supprimer les FieldScenario liés
        List<FieldScenario> fieldScenarios = fieldScenarioRepository.findByFieldId(fieldId);
        fieldScenarioRepository.deleteAll(fieldScenarios);

        // 3. Supprimer les FieldUserHistory
        List<FieldUserHistory> historyEntries = fieldUserHistoryRepository.findByFieldId(fieldId);
        fieldUserHistoryRepository.deleteAll(historyEntries);

        // 4 Supprimer les sessions de jeu liées (et toutes leurs dépendances)
        List<GameSession> sessions = gameSessionRepository.findByFieldId(fieldId);
        for (GameSession session : sessions) {
            Long sessionId = session.getId();

            // 3.1 Supprimer les scores TreasureHunt liés à la session
            treasureHuntScoreRepository.deleteAll(
                    treasureHuntScoreRepository.findByGameSessionId(sessionId)
            );

            // 3.2 Supprimer les participants
            participantRepository.deleteAll(
                    participantRepository.findByGameSessionId(sessionId)
            );

            // 3.3 Supprimer les scénarios joués dans cette session
            gameSessionScenarioRepository.deleteAll(
                    gameSessionScenarioRepository.findByGameSessionId(sessionId)
            );

            // 3.4 Supprimer la session elle-même
            gameSessionRepository.deleteById(sessionId);
        }

        // 4. Supprimer le terrain
        fieldRepository.deleteById(fieldId);
    }


}
