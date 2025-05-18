package com.airsoft.gamemapmaster.scenario.bomboperation.service.impl;

import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.repository.UserRepository;
import com.airsoft.gamemapmaster.scenario.bomboperation.exception.BombOperationException;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.*;
import com.airsoft.gamemapmaster.scenario.bomboperation.repository.BombOperationScenarioRepository;
import com.airsoft.gamemapmaster.scenario.bomboperation.repository.BombOperationSessionRepository;
import com.airsoft.gamemapmaster.scenario.bomboperation.repository.BombSiteRepository;
import com.airsoft.gamemapmaster.scenario.bomboperation.service.BombOperationPlayerStateService;
import com.airsoft.gamemapmaster.scenario.bomboperation.service.BombOperationScenarioService;
import com.airsoft.gamemapmaster.scenario.bomboperation.service.BombOperationSessionService;
import com.airsoft.gamemapmaster.scenario.bomboperation.websocket.BombOperationWebSocketNotifier;
import com.airsoft.gamemapmaster.websocket.WebSocketMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class BombOperationSessionServiceImpl implements BombOperationSessionService {

    private static final Logger logger = LoggerFactory.getLogger(BombOperationSessionServiceImpl.class);

    @Autowired
    private BombOperationSessionRepository sessionRepository;

    @Autowired
    private BombOperationScenarioRepository scenarioRepository;

    @Autowired
    private BombSiteRepository bombSiteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BombOperationScenarioService scenarioService;

    @Autowired
    private BombOperationPlayerStateService playerStateService;

    @Autowired
    private BombOperationWebSocketNotifier bombOperationWebSocketNotifier;


    @Override
    @Transactional
    public BombOperationSession createSession(Long scenarioId, Long gameSessionId) {
        logger.info("Création d'une nouvelle session pour le scénario d'Opération Bombe ID: {} et la session de jeu ID: {}",
                scenarioId, gameSessionId);

        // Vérifier si une session existe déjà pour cette session de jeu
        sessionRepository.findByGameSessionId(gameSessionId).ifPresent(session -> {
            logger.info("Une session existe déjà pour la session de jeu ID: {}, elle sera supprimée", gameSessionId);
            sessionRepository.delete(session);
        });

        BombOperationScenario scenario = scenarioService.getBombOperationScenarioById(scenarioId);

        BombOperationSession session = new BombOperationSession();
        session.setBombOperationScenario(scenario);
        session.setGameSessionId(gameSessionId);
        session.setCurrentRound(1);
        session.setAttackTeamScore(0);
        session.setDefenseTeamScore(0);
        session.setGameState(BombOperationState.WAITING);

        session = sessionRepository.save(session);
        logger.info("Session d'Opération Bombe créée avec l'ID: {}", session.getId());

        return session;
    }

    @Override
    public BombOperationSession getSessionById(Long sessionId) {
        logger.info("Récupération de la session d'Opération Bombe ID: {}", sessionId);
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> {
                    logger.error("Session d'Opération Bombe non trouvée avec l'ID: {}", sessionId);
                    return new BombOperationException.SessionNotFoundException(sessionId);
                });
    }

    @Override
    public BombOperationSession getSessionByGameSessionId(Long gameSessionId) {
        logger.info("Récupération de la session d'Opération Bombe par session de jeu ID: {}", gameSessionId);
        return sessionRepository.findByGameSessionId(gameSessionId)
                .orElseThrow(() -> {
                    logger.error("Session d'Opération Bombe non trouvée pour la session de jeu ID: {}", gameSessionId);
                    return new BombOperationException.SessionNotFoundException(gameSessionId, "game session");
                });
    }

    @Override
    @Transactional
    public BombOperationSession startRound(Long sessionId) {
        logger.info("Démarrage d'un nouveau round pour la session d'Opération Bombe ID: {}", sessionId);

        BombOperationSession session = getSessionById(sessionId);

        if (session.getGameState() != BombOperationState.WAITING && session.getGameState() != BombOperationState.ROUND_OVER) {
            logger.error("État de jeu invalide pour démarrer un round: {}", session.getGameState());
            throw new BombOperationException.InvalidGameStateException(
                    session.getGameState().toString(),
                    BombOperationState.WAITING + " ou " + BombOperationState.ROUND_OVER);
        }

        // Sélectionner aléatoirement les sites de bombe actifs
        List<BombSite> allSites = bombSiteRepository.findByBombOperationScenarioId(session.getBombOperationScenario().getId());

        if (allSites.size() < session.getBombOperationScenario().getActiveSites()) {
            logger.error("Pas assez de sites de bombe définis pour ce scénario. Requis: {}, Disponibles: {}",
                    session.getBombOperationScenario().getActiveSites(), allSites.size());
            throw new BombOperationException("Pas assez de sites de bombe définis pour ce scénario");
        }

        // Mélanger la liste et prendre les n premiers sites
        Collections.shuffle(allSites);
        List<BombSite> activeSites = allSites.subList(0, session.getBombOperationScenario().getActiveSites());

        // Mettre à jour la session
        session.setActiveBombSiteIds(activeSites.stream().map(BombSite::getId).collect(Collectors.toList()));
        session.setGameState(BombOperationState.ROUND_ACTIVE);
        session.setRoundStartTime(LocalDateTime.now());
        session.setBombPlantedTime(null);
        session.setDefuseStartTime(null);

        session = sessionRepository.save(session);
        logger.info("Round démarré pour la session d'Opération Bombe ID: {}", session.getId());

        // Envoyer une notification WebSocket
        User systemUser = userRepository.findById(1L).orElse(null); // Utilisateur système
        Long senderId = systemUser != null ? systemUser.getId() : 0L;

        WebSocketMessage message = new WebSocketMessage();
        bombOperationWebSocketNotifier.sendToGameSession(
                session.getGameSessionId(),
                BombOperationNotification.roundStart(session, senderId)
        );

        return session;
    }

    @Override
    @Transactional
    public BombOperationSession plantBomb(Long sessionId, Long userId, Long siteId, Double latitude, Double longitude) {
        logger.info("Tentative de pose de bombe par l'utilisateur ID: {} sur le site ID: {} pour la session ID: {}",
                userId, siteId, sessionId);

        BombOperationSession session = getSessionById(sessionId);

        // Vérifier l'état de la session
        if (session.getGameState() != BombOperationState.ROUND_ACTIVE) {
            logger.error("État de jeu invalide pour poser une bombe: {}", session.getGameState());
            throw new BombOperationException.InvalidGameStateException(
                    session.getGameState().toString(),
                    BombOperationState.ROUND_ACTIVE.toString());
        }

        // Vérifier que le site est actif
        if (!session.getActiveBombSiteIds().contains(siteId)) {
            logger.error("Le site de bombe ID: {} n'est pas actif pour cette session", siteId);
            throw new BombOperationException("Le site de bombe n'est pas actif pour cette session");
        }

        // Vérifier que le joueur est dans l'équipe d'attaque
        BombOperationPlayerState playerState = playerStateService.getPlayerState(sessionId, userId);

        if (playerState.getTeam() != BombOperationTeam.ATTACK) {
            logger.error("L'utilisateur ID: {} n'est pas dans l'équipe d'attaque", userId);
            throw new BombOperationException.InvalidTeamException(
                    playerState.getTeam().toString(),
                    BombOperationTeam.ATTACK.toString());
        }

        // Vérifier que le joueur est en vie
        if (!playerState.getIsAlive()) {
            logger.error("L'utilisateur ID: {} n'est pas en vie", userId);
            throw new BombOperationException.PlayerNotAliveException(userId);
        }

        // Vérifier que le joueur est dans le site de bombe
        BombSite bombSite = bombSiteRepository.findById(siteId)
                .orElseThrow(() -> new BombOperationException.BombSiteNotFoundException(siteId));

        double distance = calculateDistance(latitude, longitude, bombSite.getLatitude(), bombSite.getLongitude());

        if (distance > bombSite.getRadius()) {
            logger.error("L'utilisateur ID: {} n'est pas dans le site de bombe ID: {}", userId, siteId);
            throw new BombOperationException.NotInBombSiteException();
        }

        // Mettre à jour la session
        session.setGameState(BombOperationState.BOMB_PLANTED);
        session.setBombPlantedTime(LocalDateTime.now());

        session = sessionRepository.save(session);
        logger.info("Bombe posée sur le site ID: {} par l'utilisateur ID: {} pour la session ID: {}",
                siteId, userId, sessionId);

        // Mettre à jour le score du joueur
        playerStateService.incrementBombsPlanted(sessionId, userId);

        // Envoyer une notification WebSocket
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId));

        bombOperationWebSocketNotifier.sendToGameSession(
                session.getGameSessionId(),
                BombOperationNotification.bombPlanted(session, bombSite, user, userId)
        );

        return session;
    }

    @Override
    @Transactional
    public BombOperationSession startDefusing(Long sessionId, Long userId, Double latitude, Double longitude) {
        logger.info("Tentative de désamorçage de bombe par l'utilisateur ID: {} pour la session ID: {}",
                userId, sessionId);

        BombOperationSession session = getSessionById(sessionId);

        // Vérifier l'état de la session
        if (session.getGameState() != BombOperationState.BOMB_PLANTED) {
            logger.error("État de jeu invalide pour désamorcer une bombe: {}", session.getGameState());
            throw new BombOperationException.InvalidGameStateException(
                    session.getGameState().toString(),
                    BombOperationState.BOMB_PLANTED.toString());
        }

        // Vérifier que le joueur est dans l'équipe de défense
        BombOperationPlayerState playerState = playerStateService.getPlayerState(sessionId, userId);

        if (playerState.getTeam() != BombOperationTeam.DEFENSE) {
            logger.error("L'utilisateur ID: {} n'est pas dans l'équipe de défense", userId);
            throw new BombOperationException.InvalidTeamException(
                    playerState.getTeam().toString(),
                    BombOperationTeam.DEFENSE.toString());
        }

        // Vérifier que le joueur est en vie
        if (!playerState.getIsAlive()) {
            logger.error("L'utilisateur ID: {} n'est pas en vie", userId);
            throw new BombOperationException.PlayerNotAliveException(userId);
        }

        // Vérifier que le joueur est dans un site de bombe actif
        BombSite activeSite = isPlayerInActiveBombSite(sessionId, latitude, longitude);

        if (activeSite == null) {
            logger.error("L'utilisateur ID: {} n'est pas dans un site de bombe actif", userId);
            throw new BombOperationException.NotInBombSiteException();
        }

        // Mettre à jour la session
        session.setGameState(BombOperationState.DEFUSING);
        session.setDefuseStartTime(LocalDateTime.now());

        session = sessionRepository.save(session);
        logger.info("Désamorçage commencé par l'utilisateur ID: {} pour la session ID: {}", userId, sessionId);

        return session;
    }

    @Override
    @Transactional
    public BombOperationSession finishDefusing(Long sessionId, Long userId) {
        logger.info("Fin du désamorçage de bombe par l'utilisateur ID: {} pour la session ID: {}",
                userId, sessionId);

        BombOperationSession session = getSessionById(sessionId);

        // Vérifier l'état de la session
        if (session.getGameState() != BombOperationState.DEFUSING) {
            logger.error("État de jeu invalide pour terminer le désamorçage: {}", session.getGameState());
            throw new BombOperationException.InvalidGameStateException(
                    session.getGameState().toString(),
                    BombOperationState.DEFUSING.toString());
        }

        // Vérifier que le joueur est dans l'équipe de défense
        BombOperationPlayerState playerState = playerStateService.getPlayerState(sessionId, userId);

        if (playerState.getTeam() != BombOperationTeam.DEFENSE) {
            logger.error("L'utilisateur ID: {} n'est pas dans l'équipe de défense", userId);
            throw new BombOperationException.InvalidTeamException(
                    playerState.getTeam().toString(),
                    BombOperationTeam.DEFENSE.toString());
        }

        // Vérifier que le joueur est en vie
        if (!playerState.getIsAlive()) {
            logger.error("L'utilisateur ID: {} n'est pas en vie", userId);
            throw new BombOperationException.PlayerNotAliveException(userId);
        }

        // Vérifier le temps de désamorçage
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime defuseStartTime = session.getDefuseStartTime();

        int defuseTime = session.getBombOperationScenario().getDefuseTime();
        if (playerState.getHasDefuseKit()) {
            defuseTime /= 2; // Réduire le temps de désamorçage de moitié si le joueur a un kit
        }

        if (defuseStartTime.plusSeconds(defuseTime).isAfter(now)) {
            logger.error("Le temps de désamorçage n'est pas écoulé");
            throw new BombOperationException("Le temps de désamorçage n'est pas écoulé");
        }

        // Mettre à jour la session
        session.setGameState(BombOperationState.BOMB_DEFUSED);

        session = sessionRepository.save(session);
        logger.info("Bombe désamorcée par l'utilisateur ID: {} pour la session ID: {}", userId, sessionId);

        // Mettre à jour le score du joueur
        playerStateService.incrementBombsDefused(sessionId, userId);

        // Envoyer une notification WebSocket
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId));

        bombOperationWebSocketNotifier.sendToGameSession(
                session.getGameSessionId(),
                BombOperationNotification.bombDefused(session, user, userId)
        );

        // Terminer le round avec victoire de l'équipe de défense
        return endRound(sessionId, "DEFENSE", "Bombe désamorcée");
    }

    @Override
    @Transactional
    public BombOperationSession explodeBomb(Long sessionId) {
        logger.info("Explosion de la bombe pour la session ID: {}", sessionId);

        BombOperationSession session = getSessionById(sessionId);

        // Vérifier l'état de la session
        if (session.getGameState() != BombOperationState.BOMB_PLANTED && session.getGameState() != BombOperationState.DEFUSING) {
            logger.error("État de jeu invalide pour l'explosion de la bombe: {}", session.getGameState());
            throw new BombOperationException.InvalidGameStateException(
                    session.getGameState().toString(),
                    BombOperationState.BOMB_PLANTED + " ou " + BombOperationState.DEFUSING);
        }

        // Vérifier le temps d'explosion
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime bombPlantedTime = session.getBombPlantedTime();

        if (bombPlantedTime.plusSeconds(session.getBombOperationScenario().getBombTimer()).isAfter(now)) {
            logger.error("Le temps d'explosion n'est pas écoulé");
            throw new BombOperationException("Le temps d'explosion n'est pas écoulé");
        }

        // Mettre à jour la session
        session.setGameState(BombOperationState.BOMB_EXPLODED);

        session = sessionRepository.save(session);
        logger.info("Bombe explosée pour la session ID: {}", sessionId);

        // Envoyer une notification WebSocket
        User systemUser = userRepository.findById(1L).orElse(null); // Utilisateur système
        Long senderId = systemUser != null ? systemUser.getId() : 0L;

        bombOperationWebSocketNotifier.sendToGameSession(
                session.getGameSessionId(),
                BombOperationNotification.bombExploded(session, senderId)
        );

        // Terminer le round avec victoire de l'équipe d'attaque
        return endRound(sessionId, "ATTACK", "Bombe explosée");
    }

    @Override
    @Transactional
    public BombOperationSession endRound(Long sessionId, String winnerTeam, String reason) {
        logger.info("Fin du round pour la session ID: {} avec victoire de l'équipe: {} pour la raison: {}",
                sessionId, winnerTeam, reason);

        BombOperationSession session = getSessionById(sessionId);

        // Mettre à jour les scores
        if ("ATTACK".equals(winnerTeam)) {
            session.setAttackTeamScore(session.getAttackTeamScore() + 1);
        } else if ("DEFENSE".equals(winnerTeam)) {
            session.setDefenseTeamScore(session.getDefenseTeamScore() + 1);
        }

        // Mettre à jour l'état de la session
        session.setGameState(BombOperationState.ROUND_OVER);

        session = sessionRepository.save(session);
        logger.info("Round terminé pour la session ID: {}", sessionId);

        // Envoyer une notification WebSocket
        User systemUser = userRepository.findById(1L).orElse(null); // Utilisateur système
        Long senderId = systemUser != null ? systemUser.getId() : 0L;

        bombOperationWebSocketNotifier.sendToGameSession(
                session.getGameSessionId(),
                BombOperationNotification.roundEnd(session, winnerTeam, reason, senderId)
        );

        // Vérifier si la partie est terminée
        if (session.getCurrentRound() >= session.getBombOperationScenario().getMaxRounds() ||
                session.getAttackTeamScore() > session.getBombOperationScenario().getMaxRounds() / 2 ||
                session.getDefenseTeamScore() > session.getBombOperationScenario().getMaxRounds() / 2) {
            return endGame(sessionId);
        }

        // Préparer le prochain round
        session.setCurrentRound(session.getCurrentRound() + 1);
        session = sessionRepository.save(session);

        return session;
    }

    @Override
    @Transactional
    public BombOperationSession endGame(Long sessionId) {
        logger.info("Fin de la partie pour la session ID: {}", sessionId);

        BombOperationSession session = getSessionById(sessionId);

        // Déterminer l'équipe gagnante
        String winnerTeam;
        if (session.getAttackTeamScore() > session.getDefenseTeamScore()) {
            winnerTeam = session.getBombOperationScenario().getAttackTeamName();
        } else if (session.getDefenseTeamScore() > session.getAttackTeamScore()) {
            winnerTeam = session.getBombOperationScenario().getDefenseTeamName();
        } else {
            winnerTeam = "Match nul";
        }

        // Mettre à jour l'état de la session
        session.setGameState(BombOperationState.GAME_OVER);

        session = sessionRepository.save(session);
        logger.info("Partie terminée pour la session ID: {}", sessionId);

        // Envoyer une notification WebSocket
        User systemUser = userRepository.findById(1L).orElse(null); // Utilisateur système
        Long senderId = systemUser != null ? systemUser.getId() : 0L;

        bombOperationWebSocketNotifier.sendToGameSession(
                session.getGameSessionId(),
                BombOperationNotification.gameEnd(session, winnerTeam, senderId)
        );

        return session;
    }

    @Override
    public BombSite isPlayerInActiveBombSite(Long sessionId, Double latitude, Double longitude) {
        logger.info("Vérification si le joueur est dans un site de bombe actif pour la session ID: {}", sessionId);

        BombOperationSession session = getSessionById(sessionId);
        List<BombSite> activeSites = getActiveBombSites(sessionId);

        for (BombSite site : activeSites) {
            double distance = calculateDistance(latitude, longitude, site.getLatitude(), site.getLongitude());

            if (distance <= site.getRadius()) {
                logger.info("Le joueur est dans le site de bombe ID: {}", site.getId());
                return site;
            }
        }

        logger.info("Le joueur n'est dans aucun site de bombe actif");
        return null;
    }

    @Override
    public List<BombSite> getActiveBombSites(Long sessionId) {
        logger.info("Récupération des sites de bombe actifs pour la session ID: {}", sessionId);

        BombOperationSession session = getSessionById(sessionId);
        List<Long> activeSiteIds = session.getActiveBombSiteIds();

        if (activeSiteIds == null || activeSiteIds.isEmpty()) {
            logger.info("Aucun site de bombe actif pour la session ID: {}", sessionId);
            return new ArrayList<>();
        }

        List<BombSite> activeSites = new ArrayList<>();
        for (Long siteId : activeSiteIds) {
            bombSiteRepository.findById(siteId).ifPresent(activeSites::add);
        }

        logger.info("Récupération de {} sites de bombe actifs pour la session ID: {}", activeSites.size(), sessionId);
        return activeSites;
    }

    @Override
    @Transactional
    public void deleteSession(Long sessionId) {
        logger.info("Suppression de la session d'Opération Bombe ID: {}", sessionId);

        BombOperationSession session = getSessionById(sessionId);
        sessionRepository.delete(session);
        logger.info("Session d'Opération Bombe supprimée: {}", sessionId);
    }

    /**
     * Calcule la distance en mètres entre deux points géographiques
     * @param lat1 Latitude du premier point
     * @param lon1 Longitude du premier point
     * @param lat2 Latitude du deuxième point
     * @param lon2 Longitude du deuxième point
     * @return Distance en mètres
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Rayon de la Terre en km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c * 1000; // Distance en mètres
    }
}
