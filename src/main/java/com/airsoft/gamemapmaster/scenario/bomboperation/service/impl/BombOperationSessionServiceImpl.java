package com.airsoft.gamemapmaster.scenario.bomboperation.service.impl;

import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.repository.UserRepository;
import com.airsoft.gamemapmaster.scenario.bomboperation.dto.BombOperationSessionDto;
import com.airsoft.gamemapmaster.scenario.bomboperation.dto.BombSiteDto;
import com.airsoft.gamemapmaster.scenario.bomboperation.exception.BombOperationException;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.*;
import com.airsoft.gamemapmaster.scenario.bomboperation.repository.BombOperationScenarioRepository;
import com.airsoft.gamemapmaster.scenario.bomboperation.repository.BombOperationSessionRepository;
import com.airsoft.gamemapmaster.scenario.bomboperation.repository.BombOperationTeamRoleRepository;
import com.airsoft.gamemapmaster.scenario.bomboperation.repository.BombSiteRepository;
import com.airsoft.gamemapmaster.scenario.bomboperation.service.BombOperationPlayerStateService;
import com.airsoft.gamemapmaster.scenario.bomboperation.service.BombOperationScenarioService;
import com.airsoft.gamemapmaster.scenario.bomboperation.service.BombOperationSessionService;
import com.airsoft.gamemapmaster.scenario.bomboperation.service.BombSiteSessionStateService;
import com.airsoft.gamemapmaster.scenario.bomboperation.websocket.BombOperationWebSocketNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BombOperationSessionServiceImpl implements BombOperationSessionService {

    private static final Logger logger = LoggerFactory.getLogger(BombOperationSessionServiceImpl.class);

    @Autowired
    private BombOperationSessionRepository bombOperationSessionRepository;

    @Autowired
    private BombOperationScenarioRepository scenarioRepository;

    @Autowired
    private BombSiteRepository bombSiteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BombOperationScenarioService bombOperationScenarioService;

    @Autowired
    private BombOperationPlayerStateService bombOperationPlayerStateService;

    @Autowired
    private BombOperationWebSocketNotifier bombOperationWebSocketNotifier;

    @Autowired
    private BombOperationTeamRoleRepository bombOperationTeamRoleRepository;

    @Autowired
    private BombSiteSessionStateService bombSiteSessionStateService;


    @Override
    @Transactional
    public BombOperationSessionDto createBombOperationSession(Long scenarioId, Long gameSessionId) {
        logger.info("Création d'une nouvelle session pour le scénario d'Opération Bombe ID: {} et la session de jeu ID: {}",
                scenarioId, gameSessionId);

        // Vérifier si une session existe déjà pour cette session de jeu
        bombOperationSessionRepository.findByGameSessionId(gameSessionId).ifPresent(session -> {
            logger.info("Une session existe déjà pour la session de jeu ID: {}, elle sera supprimée", gameSessionId);
            // Supprimer aussi les états de sites associés
            bombSiteSessionStateService.deleteAllSessionStates(gameSessionId);
            bombOperationSessionRepository.delete(session);
        });

        BombOperationScenario bombOperationScenario = bombOperationScenarioService.getBombOperationScenarioByScenarioId(scenarioId);

        BombOperationSession bombOperationSession = new BombOperationSession();
        bombOperationSession.setBombOperationScenario(bombOperationScenario);
        bombOperationSession.setGameSessionId(gameSessionId);
        bombOperationSession.setCurrentRound(1);
        bombOperationSession.setAttackTeamScore(0);
        bombOperationSession.setDefenseTeamScore(0);
        bombOperationSession.setGameState(BombOperationState.WAITING);

        bombOperationSession = bombOperationSessionRepository.save(bombOperationSession);
        logger.info("Session d'Opération Bombe créée avec l'ID: {}", bombOperationSession.getId());

        // ✨ Créer automatiquement les BombSiteSessionState
        Set<BombSite> allBombSites = bombOperationScenario.getBombSites();
        Map<Long, BombSite> bombSiteMap = allBombSites.stream()
                .collect(Collectors.toMap(BombSite::getId, site -> site));

        List<BombSiteSessionState> sessionStates = bombSiteSessionStateService.createSessionStatesFromBombSites(
                gameSessionId, new ArrayList<>(allBombSites));

        // ✨ Sélectionner et activer aléatoirement les sites selon le scénario
        int numberOfSitesToActivate = bombOperationScenario.getActiveSites();
        List<BombSiteSessionState> activatedSites = bombSiteSessionStateService.selectAndActivateRandomSites(
                gameSessionId, numberOfSitesToActivate);

        logger.info("✅ {} BombSiteSessionState créés, {} sites activés aléatoirement",
                sessionStates.size(), activatedSites.size());

        // Récupération explicite des rôles associés à cette session
        List<BombOperationTeamRole> roles = bombOperationTeamRoleRepository.findByGameSessionId(gameSessionId);
        // Convertir vers un map
        Map<Long, String> teamRoles = new HashMap<>();
        for (BombOperationTeamRole role : roles) {
            teamRoles.put(role.getTeamId(), role.getRole());
        }

        // ✨ NOUVELLE LOGIQUE : Utiliser les BombSiteSessionState pour construire les listes
        // Sites à activer = tous les sites ACTIVE
        List<BombSiteSessionState> activeSites = bombSiteSessionStateService.getActiveSites(gameSessionId);
        List<BombSiteDto> toActiveBombSitesDto = new ArrayList<>();
        for (BombSiteSessionState state : activeSites) {
            BombSite originalSite = bombSiteMap.get(state.getOriginalBombSiteId());
            if (originalSite != null) {
                toActiveBombSitesDto.add(convertSessionStateToDto(state, originalSite));
            }
        }

        // Sites désactivés = tous les sites INACTIVE
        List<BombSiteDto> disableBombSitesDto = new ArrayList<>();
        for (BombSite bombSite : allBombSites) {
            disableBombSitesDto.add(bombSite.toDto());
        }

        // Attacher ce map à une DTO enrichie
        BombOperationSessionDto dto = bombOperationSession.toDto(teamRoles);
        dto.setToActiveBombSites(toActiveBombSitesDto);
        dto.setDisableBombSites(disableBombSitesDto);
        dto.setActiveBombSites(new ArrayList<>());
        dto.setExplodedBombSites(new ArrayList<>());

        logger.info("Session d'Opération Bombe avec {} sites actifs et {} sites total créés via BombSiteSessionState",
                toActiveBombSitesDto.size(), disableBombSitesDto.size());

        return dto;
    }

    @Override
    public BombOperationSession getSessionById(Long sessionId) {
        logger.info("Récupération de la session d'Opération Bombe ID: {}", sessionId);
        return bombOperationSessionRepository.findById(sessionId)
                .orElseThrow(() -> {
                    logger.error("Session d'Opération Bombe non trouvée avec l'ID: {}", sessionId);
                    return new BombOperationException.SessionNotFoundException(sessionId);
                });
    }

    @Override
    public BombOperationSessionDto getBombOperationSessionDtoByGameSessionId(Long gameSessionId) {
        logger.info("Récupération de la session d'Opération Bombe par session de jeu ID: {}", gameSessionId);

        BombOperationSession bombOperationSession = bombOperationSessionRepository.findByGameSessionId(gameSessionId)
                .orElseThrow(() -> {
                    logger.error("Session d'Opération Bombe non trouvée pour la session de jeu ID: {}", gameSessionId);
                    return new BombOperationException.SessionNotFoundException(gameSessionId, "game session");
                });

        logger.info("Session d'Opération Bombe trouvée pour la session de jeu ID: {}", gameSessionId);

        // 🧠 Récupération des rôles
        List<BombOperationTeamRole> teamRoles = bombOperationTeamRoleRepository.findByGameSessionId(gameSessionId);
        Map<Long, String> rolesMap = new HashMap<>();
        for (BombOperationTeamRole role : teamRoles) {
            rolesMap.put(role.getTeamId(), role.getRole());
        }

        // 🧠 Préparer l'accès aux BombSites originaux
        BombOperationScenario scenario = bombOperationSession.getBombOperationScenario();
        Set<BombSite> allBombSites = scenario.getBombSites();
        Map<Long, BombSite> bombSiteMap = allBombSites.stream()
                .collect(Collectors.toMap(BombSite::getId, site -> site));

        // 🧠 Enrichissement du DTO
        BombOperationSessionDto dto = bombOperationSession.toDto(rolesMap);

        // Sites actifs
        List<BombSiteSessionState> toActiveStates = bombSiteSessionStateService.getActiveSites(gameSessionId);
        List<BombSiteDto> toActiveBombSitesDto = new ArrayList<>();
        for (BombSiteSessionState state : toActiveStates) {
            BombSite original = bombSiteMap.get(state.getOriginalBombSiteId());
            if (original != null) {
                toActiveBombSitesDto.add(convertSessionStateToDto(state, original));
            }
        }

        List<BombSiteSessionState> armedStates = bombSiteSessionStateService.getArmedSites(gameSessionId);
        List<BombSiteDto> armedBombSitesDto = new ArrayList<>();
        for (BombSiteSessionState state : armedStates) {
            BombSite original = bombSiteMap.get(state.getOriginalBombSiteId());
            if (original != null) {
                armedBombSitesDto.add(convertSessionStateToDto(state, original));
            }
        }

            // Sites explosés
        List<BombSiteSessionState> explodedStates = bombSiteSessionStateService.getExplodedSites(gameSessionId);
        List<BombSiteDto> explodedBombSitesDto = new ArrayList<>();
        for (BombSiteSessionState state : explodedStates) {
            BombSite original = bombSiteMap.get(state.getOriginalBombSiteId());
            if (original != null) {
                explodedBombSitesDto.add(convertSessionStateToDto(state, original));
            }
        }

        // Sites désactivés = tous les sites définis dans le scénario
        List<BombSiteDto> disableBombSitesDto = new ArrayList<>();
        for (BombSite site : allBombSites) {
            disableBombSitesDto.add(site.toDto());
        }

        // Remplir le DTO avec les listes correctement enrichies
        dto.setActiveBombSites(armedBombSitesDto);
        dto.setExplodedBombSites(explodedBombSitesDto);
        dto.setToActiveBombSites(toActiveBombSitesDto);
        dto.setDisableBombSites(disableBombSitesDto);

        logger.info("✅ DTO enrichi via BombSiteSessionState : a activer={}, amorcé={},explosés={}, désactivés={}",
                toActiveBombSitesDto.size(),armedBombSitesDto.size(), explodedBombSitesDto.size(), disableBombSitesDto.size());

        return dto;
    }

    @Override
    public BombOperationSession getBombOperationSessionByGameSessionId(Long gameSessionId) {
        logger.info("Récupération de la session d'Opération Bombe par session de jeu ID: {}", gameSessionId);
        BombOperationSession bombOperationSession = bombOperationSessionRepository.findByGameSessionId(gameSessionId)
                .orElse(null);
        if (bombOperationSession == null) {
            logger.error("Session d'Opération Bombe non trouvée pour la session de jeu ID: {}", gameSessionId);
            throw new BombOperationException.SessionNotFoundException(gameSessionId, "game session");
        }
        logger.info("Session d'Opération Bombe trouvée pour la session de jeu ID: {}", gameSessionId);
        return bombOperationSession;
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
        BombOperationPlayerState playerState = bombOperationPlayerStateService.getPlayerState(sessionId, userId);

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

        session = bombOperationSessionRepository.save(session);
        logger.info("Bombe posée sur le site ID: {} par l'utilisateur ID: {} pour la session ID: {}",
                siteId, userId, sessionId);

        // Mettre à jour le score du joueur
        bombOperationPlayerStateService.incrementBombsPlanted(sessionId, userId);

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
        BombOperationPlayerState playerState = bombOperationPlayerStateService.getPlayerState(sessionId, userId);

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

        session = bombOperationSessionRepository.save(session);
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
        BombOperationPlayerState playerState = bombOperationPlayerStateService.getPlayerState(sessionId, userId);

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
        session.setLastUpdated(LocalDateTime.now());

        session = bombOperationSessionRepository.save(session);
        logger.info("Bombe désarmée avec succès par l'utilisateur ID: {} pour la session ID: {}", userId, sessionId);

        // Mettre à jour le score du joueur
        bombOperationPlayerStateService.incrementBombsDefused(sessionId, userId);

        // Envoyer une notification WebSocket
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId));

        //BombOperationNotification.roundEnd(session, "DEFENSE", "Bombe désamorcée", senderId)
        bombOperationWebSocketNotifier.sendToGameSession(
                session.getGameSessionId(),
                BombOperationNotification.bombDefused(session, user, userId)
        );

        // Notifier la fin du round avec victoire de l'équipe de défense
        User systemUser = userRepository.findById(1L).orElse(null); // Utilisateur système
        Long senderId = systemUser != null ? systemUser.getId() : 0L;

        return session;
    }

    @Override
    @Transactional
    public BombOperationSession explodeBomb(Long sessionId) {
        logger.info("Explosion de bombe pour la session ID: {}", sessionId);

        BombOperationSession session = getSessionById(sessionId);

        // Vérifier l'état de la session
        if (session.getGameState() != BombOperationState.BOMB_PLANTED) {
            logger.error("État de jeu invalide pour faire exploser une bombe: {}", session.getGameState());
            throw new BombOperationException.InvalidGameStateException(
                    session.getGameState().toString(),
                    BombOperationState.BOMB_PLANTED.toString());
        }

        // Mettre à jour la session
        session.setGameState(BombOperationState.BOMB_EXPLODED);
        session.setLastUpdated(LocalDateTime.now());

        session = bombOperationSessionRepository.save(session);
        logger.info("Bombe explosée pour la session ID: {}", sessionId);

        // Envoyer une notification WebSocket
        User systemUser = userRepository.findById(1L).orElse(null); // Utilisateur système
        Long senderId = systemUser != null ? systemUser.getId() : 0L;

        //BombOperationNotification.roundEnd(session, "ATTACK", "Bombe explosée", senderId)

        bombOperationWebSocketNotifier.sendToGameSession(
                session.getGameSessionId(),
                BombOperationNotification.bombExploded(session, senderId)
        );

        // Terminer le round avec victoire de l'équipe d'attaque
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

        session = bombOperationSessionRepository.save(session);
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

        // Supprimer les états de sites associés
        bombSiteSessionStateService.deleteAllSessionStates(session.getGameSessionId());

        bombOperationSessionRepository.delete(session);
        logger.info("Session d'Opération Bombe ID: {} supprimée avec succès", sessionId);
    }

    @Override
    public Object getGameSessionState(Long gameSessionId) {
        logger.info("Récupération de l'état de la session de jeu ID: {}", gameSessionId);
        return getBombOperationSessionDtoByGameSessionId(gameSessionId);
    }

    /**
     * Sauvegarde les rôles des équipes pour une session de jeu
     */
    @Transactional
    @Override
    public void saveTeamRoles(Long gameSessionId, Map<String, String> teamRoles) {
        // Supprimer les rôles existants pour cette session
        bombOperationTeamRoleRepository.deleteByGameSessionId(gameSessionId);

        // Sauvegarder les nouveaux rôles
        for (Map.Entry<String, String> entry : teamRoles.entrySet()) {
            Long teamId = Long.parseLong(entry.getKey());
            String role = entry.getValue();

            BombOperationTeamRole teamRole = new BombOperationTeamRole();
            teamRole.setGameSessionId(gameSessionId);
            teamRole.setTeamId(teamId);
            teamRole.setRole(role);

            bombOperationTeamRoleRepository.save(teamRole);
        }
    }

    @Override
    @Transactional
    public BombOperationSession bombArmed(Long sessionId, Long userId, Long siteId, Double latitude, Double longitude) {
        logger.info("Notification d'armement de bombe terminé pour la session ID: {}, utilisateur ID: {}, site ID: {}",
                sessionId, userId, siteId);

        BombOperationSession session = getSessionById(sessionId);
        Long gameSessionId = session.getGameSessionId();

        // ✨ NOUVELLE LOGIQUE : Utiliser BombSiteSessionState
        try {
            // Récupérer le timer de bombe du scénario
            Integer bombTimer = session.getBombOperationScenario().getBombTimer();

            // Armer la bombe via le service BombSiteSessionState
            BombSiteSessionState armedSite = bombSiteSessionStateService.armBomb(
                    gameSessionId, siteId, userId, bombTimer);

            // Mettre à jour l'état de la session
            session.setGameState(BombOperationState.BOMB_PLANTED);
            session.setBombPlantedTime(LocalDateTime.now());
            session.setLastUpdated(LocalDateTime.now());

            session = bombOperationSessionRepository.save(session);

            // Envoyer notification WebSocket
/*            bombOperationWebSocketNotifier.sendBombPlantedNotification(
                    sessionId,
                    userId,
                    siteId,
                    armedSite.getName(),
                    bombTimer
            );*/

            logger.info("✅ Bombe armée avec succès sur le site '{}' (ID: {}) par le joueur ID: {}",
                    armedSite.getName(), siteId, userId);

        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'armement de la bombe: {}", e.getMessage(), e);
            throw new BombOperationException("Erreur lors de l'armement de la bombe: " + e.getMessage());
        }

        logger.info("Bombe armée avec succès sur le site ID: {} par le joueur ID: {}", siteId, userId);
        return session;
    }

    @Override
    @Transactional
    public BombOperationSession bombDisarmed(Long sessionId, Long userId, Long siteId, Double latitude, Double longitude) {
        logger.info("Notification de désarmement de bombe terminé pour la session ID: {}, utilisateur ID: {}, site ID: {}",
                sessionId, userId, siteId);

        BombOperationSession session = getSessionById(sessionId);
        Long gameSessionId = session.getGameSessionId();

        // ✨ NOUVELLE LOGIQUE : Utiliser BombSiteSessionState
        try {
            // Vérifier que la bombe était bien armée
            if (session.getGameState() != BombOperationState.BOMB_PLANTED) {
                logger.error("Tentative de désarmement alors qu'aucune bombe n'est armée pour la session ID: {}", sessionId);
                throw new BombOperationException("Aucune bombe n'est actuellement armée");
            }

            // Désarmer la bombe via le service BombSiteSessionState
            BombSiteSessionState disarmedSite = bombSiteSessionStateService.disarmBomb(
                    gameSessionId, siteId, userId);

            // Mettre à jour l'état de la session
            session.setGameState(BombOperationState.BOMB_DEFUSED);
            session.setDefuseStartTime(LocalDateTime.now()); // Utilisé comme temps de fin de désarmement
            session.setLastUpdated(LocalDateTime.now());

            session = bombOperationSessionRepository.save(session);

            // Envoyer notification WebSocket
/*
            bombOperationWebSocketNotifier.sendDefuseSuccessNotification(sessionId, userId);
*/

            logger.info("✅ Bombe désarmée avec succès sur le site '{}' (ID: {}) par le joueur ID: {}",
                    disarmedSite.getName(), siteId, userId);

        } catch (Exception e) {
            logger.error("❌ Erreur lors du désarmement de la bombe: {}", e.getMessage(), e);
            throw new BombOperationException("Erreur lors du désarmement de la bombe: " + e.getMessage());
        }

        logger.info("Bombe désarmée avec succès sur le site ID: {} par le joueur ID: {}", siteId, userId);
        return session;
    }


    /**
     * Récupère les rôles des équipes pour une session de jeu
     */
    @Override
    public Map<String, String> getTeamRoles(Long gameSessionId) {
        List<BombOperationTeamRole> teamRoles = bombOperationTeamRoleRepository.findByGameSessionId(gameSessionId);
        Map<String, String> result = new HashMap<>();

        for (BombOperationTeamRole teamRole : teamRoles) {
            result.put(teamRole.getTeamId().toString(), teamRole.getRole());
        }

        return result;
    }

    @Override
    public List<BombSite> selectAndActivateRandomSites(Long gameSessionId) {
        BombOperationSession bombOperationSession = bombOperationSessionRepository.findByGameSessionId(gameSessionId)
                .orElseThrow(() -> new RuntimeException("Session non trouvée"));

        BombOperationScenario scenario = bombOperationSession.getBombOperationScenario();

        int toActivate = scenario.getActiveSites() != null && scenario.getActiveSites() > 0
                ? Math.min(scenario.getActiveSites(), scenario.getBombSites().size())
                : 1;

        List<BombSite> allSites = new ArrayList<>(scenario.getBombSites());
        Collections.shuffle(allSites);

        List<BombSite> selectedSites = allSites.subList(0, toActivate);

        // Réinitialiser tous les sites
        for (BombSite site : scenario.getBombSites()) {
            site.setActive(false);
        }

        // Activer les sites choisis
        for (BombSite site : selectedSites) {
            site.setActive(true);
        }

        // Sauvegarde en cascade si nécessaire (sinon saveAll)
        bombSiteRepository.saveAll(scenario.getBombSites());

        // Mémorisation dans la session
        List<Long> selectedIds = new ArrayList<>();
        for (BombSite site : selectedSites) {
            selectedIds.add(site.getId());
        }
        bombOperationSession.setActiveBombSiteIds(selectedIds);
        bombOperationSessionRepository.save(bombOperationSession);

        return selectedSites;
    }


    private int calculateRemainingTime(BombOperationSession session) {
        if (session.getGameState() != BombOperationState.BOMB_PLANTED) return 0;

        int totalSeconds = session.getBombOperationScenario().getBombTimer();
        long elapsed = java.time.Duration.between(session.getBombPlantedTime(), LocalDateTime.now()).getSeconds();
        return Math.max(0, totalSeconds - (int) elapsed);
    }

    /**
     * Calcule la distance en mètres entre deux points géographiques
     *
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

    /**
     * Convertit un BombSiteSessionState en BombSiteDto
     *
     * @param sessionState L'état de session à convertir
     * @return Le DTO correspondant
     */
    private BombSiteDto convertSessionStateToDto(BombSiteSessionState sessionState, BombSite site) {
        BombSiteDto dto = new BombSiteDto();
        dto.setId(sessionState.getOriginalBombSiteId());
        dto.setName(sessionState.getName());
        dto.setLatitude(sessionState.getLatitude());
        dto.setLongitude(sessionState.getLongitude());
        dto.setRadius(sessionState.getRadius());
        BombSiteStatus status = sessionState.getStatus();
        boolean active = (status == BombSiteStatus.ARMED);
        dto.setActive(active);
        dto.setBombOperationScenarioId(site.getBombOperationScenario().getId());
        dto.setScenarioId(site.getScenarioId());
        return dto;
    }
}

