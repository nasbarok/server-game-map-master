package com.airsoft.gamemapmaster.service.impl;

import com.airsoft.gamemapmaster.controller.FieldController;
import com.airsoft.gamemapmaster.model.*;
import com.airsoft.gamemapmaster.repository.*;
import com.airsoft.gamemapmaster.scenario.treasurehunt.model.TreasureHuntScenario;
import com.airsoft.gamemapmaster.scenario.treasurehunt.model.TreasureHuntScore;
import com.airsoft.gamemapmaster.scenario.treasurehunt.repository.TreasureHuntScoreRepository;
import com.airsoft.gamemapmaster.scenario.treasurehunt.service.TreasureHuntService;
import com.airsoft.gamemapmaster.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class GameSessionServiceImpl implements GameSessionService {
    private static final Logger log = LoggerFactory.getLogger(GameSessionServiceImpl.class);

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private GameSessionParticipantRepository participantRepository;

    @Autowired
    private GameSessionScenarioRepository scenarioRepository;

    @Autowired
    private GameMapRepository gameMapRepository;

    @Autowired
    private FieldScenarioRepository fieldScenarioRepository;
    @Autowired
    private UserService userService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private ScenarioService scenarioService;

    @Autowired
    private ConnectedPlayerService connectedPlayerService;

    @Autowired
    private TreasureHuntService treasureHuntService;

    @Autowired
    private TreasureHuntScoreRepository treasureHuntScoreRepository;




    @Override
    public GameSession createGameSession(GameSession gameSession) {
        //set gameMap id
        List<GameMap> gameMaps = gameMapRepository.findByFieldId(gameSession.getField().getId());
        if (!gameMaps.isEmpty()) {
            gameSession.setGameMap(gameMaps.get(0));
        }

        return gameSessionRepository.save(gameSession);
    }

    @Override
    @Transactional
    public GameSession startGameSession(Long gameSessionId, LocalDateTime startTime) {
        GameSession gameSession = gameSessionRepository.findById(gameSessionId)
                .orElseThrow(() -> new RuntimeException("Game session not found with id: " + gameSessionId));

        // ⏱️ Mise à jour des métadonnées de session
        gameSession.setStartTime(startTime);
        gameSession.setActive(true);
        GameSession savedSession = gameSessionRepository.save(gameSession);

        // 📥 Récupération des joueurs connectés au terrain
        Long fieldId = savedSession.getField().getId();
        List<ConnectedPlayer> connectedPlayers = connectedPlayerService.getConnectedPlayersByFieldId(fieldId);

        // 🧱 Création des participants
        List<GameSessionParticipant> participants = new ArrayList<>();
        for (int i = 0; i < connectedPlayers.size(); i++) {
            ConnectedPlayer cp = connectedPlayers.get(i);
            GameSessionParticipant participant = new GameSessionParticipant();
            participant.setGameSession(savedSession);
            participant.setUser(cp.getUser());
            participant.setUserUsername(cp.getUser().getUsername());
            participant.setTeam(cp.getTeam());
            participant.setTeamName(cp.getTeam() != null ? cp.getTeam().getName() : null);
            participant.setParticipantType("PLAYER");
            participants.add(participant);

            log.info("👤 Participant ajouté : " + participant.getUserUsername() +
                    (participant.getTeamName() != null ? " (équipe: " + participant.getTeamName() + ")" : ""));
        }
        participantRepository.saveAll(participants);
        savedSession.getParticipants().addAll(participants);
        log.info("✅ " + participants.size() + " participants enregistrés pour la session ID=" + savedSession.getId());

        // 📦 Récupération des scénarios liés
        List<FieldScenario> fieldScenarios = fieldScenarioRepository.findByFieldId(fieldId);
        List<GameSessionScenario> sessionScenarios = new ArrayList<>();

        for (FieldScenario fieldScenario : fieldScenarios) {
            Scenario scenario = fieldScenario.getScenario();
            scenario.setActive(true);
            scenarioService.save(scenario);
            GameSessionScenario gss = new GameSessionScenario();
            gss.setGameSession(savedSession);
            gss.setScenario(scenario);
            gss.setScenarioType(scenario.getType());
            gss.setActive(true);
            gss.setIsMainScenario(false);
            sessionScenarios.add(gss);

            log.info("🧩 Scénario ajouté : {} (type: {})", scenario.getName(), scenario.getType());
        }

        scenarioRepository.saveAll(sessionScenarios);
        savedSession.getScenarios().addAll(sessionScenarios);

        log.info("✅ {} scénarios liés à la session ID={}", sessionScenarios.size(), savedSession.getId());

        // 🧮 Initialisation des scores Treasure Hunt
        for (GameSessionScenario gss : sessionScenarios) {
            if ("treasure_hunt".equalsIgnoreCase(gss.getScenarioType())) {
                Optional<TreasureHuntScenario> treasureHuntOpt = treasureHuntService.findByScenarioId(gss.getScenario().getId());

                if (treasureHuntOpt.isPresent()) {
                    TreasureHuntScenario thScenario = treasureHuntOpt.get();

                    for (GameSessionParticipant participant : participants) {
                        TreasureHuntScore score = new TreasureHuntScore();
                        score.setTreasureHuntScenario(thScenario);
                        score.setUser(participant.getUser());
                        score.setTeam(participant.getTeam());
                        score.setGameSessionId(savedSession.getId());
                        score.setScore(0);
                        score.setTreasuresFound(0);

                        // Enregistrer le score
                        treasureHuntScoreRepository.save(score);

                        log.info("📊 Score TreasureHunt initialisé pour {} (userId={}, teamId={}, scenarioId={})",
                                participant.getUserUsername(),
                                participant.getUser().getId(),
                                participant.getTeam() != null ? participant.getTeam().getId() : null,
                                thScenario.getId()
                        );
                    }
                } else {
                    log.warn("❌ Aucun TreasureHuntScenario trouvé pour le scénario ID={}", gss.getScenario().getId());
                }
            }
        }

        return savedSession;
    }

    @Override
    @Transactional
    public GameSession endGameSession(Long gameSessionId,LocalDateTime endTime) {
        return gameSessionRepository.findById(gameSessionId)
                .map(gameSession -> {
                    gameSession.setEndTime(endTime);
                    gameSession.setActive(false);
                    return gameSessionRepository.save(gameSession);
                })
                .orElseThrow(() -> new RuntimeException("Game session not found with id: " + gameSessionId));
    }

    @Override
    public Optional<GameSession> findById(Long sessionId) {
        return gameSessionRepository.findById(sessionId);
    }

    @Override
    public Optional<GameSession> findActiveSessionByFieldId(Long fieldId) {
        return gameSessionRepository.findFirstByFieldIdAndActiveTrueOrderByStartTimeDesc(fieldId);
    }

    @Override
    public Optional<GameSession> findActiveGameSession(Long gameSessionId) {
        return Optional.empty();
    }

    @Override
    public List<GameSession> findAllActiveGameSessions() {
        return gameSessionRepository.findByActiveTrue();
    }

    @Override
    public List<GameSession> findByGameMapId(Long gameMapId) {
        return null;
    }

    @Override
    @Transactional
    public GameSessionParticipant addParticipant(Long gameSessionId, Long userId, Long teamId, Boolean isHost) {
        GameSession gameSession = gameSessionRepository.findById(gameSessionId)
                .orElseThrow(() -> new RuntimeException("Game session not found with id: " + gameSessionId));

        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Team team = null;
        if (teamId != null) {
            team = teamService.findById(teamId)
                    .orElseThrow(() -> new RuntimeException("Team not found with id: " + teamId));
        }

        // Vérifier si le participant existe déjà
        Optional<GameSessionParticipant> existingParticipant = participantRepository.findByGameSessionIdAndUserId(gameSessionId, userId);

        if (existingParticipant.isPresent()) {
            GameSessionParticipant participant = existingParticipant.get();
            if (participant.getLeftAt() != null) {
                // Le participant avait quitté, on le fait revenir
                participant.setLeftAt(null);
                participant.setCreatedAt(LocalDateTime.now());
                if (team != null) {
                    participant.setTeam(team);
                }
                return participantRepository.save(participant);
            } else {
                // Le participant est déjà actif
                return participant;
            }
        } else {
            // Créer un nouveau participant
            GameSessionParticipant participant = new GameSessionParticipant();
            participant.setGameSession(gameSession);
            participant.setUser(user);
            participant.setTeam(team);
            participant.setCreatedAt(LocalDateTime.now());
            return participantRepository.save(participant);
        }
    }

    @Override
    @Transactional
    public GameSessionParticipant removeParticipant(Long gameSessionId, Long userId) {
        return participantRepository.findByGameSessionIdAndUserId(gameSessionId, userId)
                .map(participant -> {
                    participant.setLeftAt(LocalDateTime.now());
                    return participantRepository.save(participant);
                })
                .orElseThrow(() -> new RuntimeException("Participant not found for game session id: " + gameSessionId + " and user id: " + userId));
    }

    @Override
    public List<GameSessionParticipant> getParticipants(Long gameSessionId) {
        return participantRepository.findByGameSessionId(gameSessionId);
    }

    @Override
    public List<GameSessionParticipant> getActiveParticipants(Long gameSessionId) {
        return participantRepository.findByGameSessionIdAndLeftAtIsNull(gameSessionId);
    }

    @Override
    public Optional<GameSessionParticipant> findParticipant(Long gameSessionId, Long userId) {
        return participantRepository.findByGameSessionIdAndUserId(gameSessionId, userId);
    }

    @Override
    @Transactional
    public GameSessionScenario addScenario(Long gameSessionId, Long scenarioId, Boolean isMainScenario) {
        GameSession gameSession = gameSessionRepository.findById(gameSessionId)
                .orElseThrow(() -> new RuntimeException("Game session not found with id: " + gameSessionId));

        Scenario scenario = scenarioService.findById(scenarioId)
                .orElseThrow(() -> new RuntimeException("Scenario not found with id: " + scenarioId));

        // Vérifier si le scénario existe déjà
        Optional<GameSessionScenario> existingScenario = scenarioRepository.findByGameSessionIdAndScenarioId(gameSessionId, scenarioId);

        if (existingScenario.isPresent()) {
            return existingScenario.get();
        } else {
            // Si c'est un scénario principal et qu'il y a déjà un scénario principal, lever une exception
            if (Boolean.TRUE.equals(isMainScenario)) {
                List<GameSessionScenario> mainScenarios = scenarioRepository.findByGameSessionIdAndIsMainScenarioTrue(gameSessionId);
                if (!mainScenarios.isEmpty()) {
                    throw new RuntimeException("A main scenario already exists for this game session");
                }
            }

            // Créer un nouveau scénario de session
            GameSessionScenario gameSessionScenario = new GameSessionScenario();
            gameSessionScenario.setGameSession(gameSession);
            gameSessionScenario.setScenario(scenario);
            gameSessionScenario.setActive(false);
            gameSessionScenario.setScenarioType(scenario.getType());
            gameSessionScenario.setIsMainScenario(isMainScenario != null ? isMainScenario : false);
            return scenarioRepository.save(gameSessionScenario);
        }
    }

    @Override
    @Transactional
    public GameSessionScenario activateScenario(Long gameSessionId, Long scenarioId) {
        return scenarioRepository.findByGameSessionIdAndScenarioId(gameSessionId, scenarioId)
                .map(gameSessionScenario -> {
                    gameSessionScenario.setActive(true);
                    return scenarioRepository.save(gameSessionScenario);
                })
                .orElseThrow(() -> new RuntimeException("Scenario not found for game session id: " + gameSessionId + " and scenario id: " + scenarioId));
    }

    @Override
    @Transactional
    public GameSessionScenario deactivateScenario(Long gameSessionId, Long scenarioId) {
        return scenarioRepository.findByGameSessionIdAndScenarioId(gameSessionId, scenarioId)
                .map(gameSessionScenario -> {
                    gameSessionScenario.setActive(false);
                    return scenarioRepository.save(gameSessionScenario);
                })
                .orElseThrow(() -> new RuntimeException("Scenario not found for game session id: " + gameSessionId + " and scenario id: " + scenarioId));
    }

    @Override
    public List<GameSessionScenario> getScenarios(Long gameSessionId) {
        return scenarioRepository.findByGameSessionId(gameSessionId);
    }

    @Override
    public List<GameSessionScenario> getActiveScenarios(Long gameSessionId) {
        return scenarioRepository.findByGameSessionIdAndActiveTrue(gameSessionId);
    }

    @Override
    public Optional<GameSessionScenario> findScenario(Long gameSessionId, Long scenarioId) {
        return scenarioRepository.findByGameSessionIdAndScenarioId(gameSessionId, scenarioId);
    }

    @Override
    public long getRemainingTimeInSeconds(Long gameSessionId) {
        return gameSessionRepository.findById(gameSessionId)
                .map(GameSession::getRemainingTimeInSeconds)
                .orElse(0L);
    }


    @Override
    public Long getCurrentGameSessionId() {
        return null;
    }

    @Override
    public GameSession startNewSession(Field field) {
        GameSession gameSession = new GameSession();
        gameSession.setField(field);
        gameSession.setStartTime(LocalDateTime.now());
        gameSession.setActive(true);
        return gameSessionRepository.save(gameSession);
    }

    @Override
    public boolean isGameSessionActive(Long gameSessionId) {
        Optional<GameSession> gameSession =  gameSessionRepository.findById(gameSessionId);
        if (gameSession.isPresent()) {
            return gameSession.get().getActive();
        }
        return false;
    }

    @Override
    @Scheduled(fixedRate = 60000) // Vérifier toutes les minutes
    @Transactional
    public void checkAndEndExpiredGameSessions() {
        List<GameSession> activeSessions = gameSessionRepository.findByActiveTrue();
        LocalDateTime now = LocalDateTime.now();

        for (GameSession session : activeSessions) {
            if (session.isExpired()) {
                session.setEndTime(now);
                session.setActive(false);
                gameSessionRepository.save(session);
            }
        }
    }

}
