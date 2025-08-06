package com.airsoft.gamemapmaster.scenario.bomboperation.service.impl;

import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.repository.UserRepository;
import com.airsoft.gamemapmaster.scenario.bomboperation.exception.BombOperationException;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombOperationPlayerState;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombOperationScore;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombOperationSession;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombOperationTeam;
import com.airsoft.gamemapmaster.scenario.bomboperation.repository.BombOperationPlayerStateRepository;
import com.airsoft.gamemapmaster.scenario.bomboperation.repository.BombOperationScoreRepository;
import com.airsoft.gamemapmaster.scenario.bomboperation.repository.BombOperationSessionRepository;
import com.airsoft.gamemapmaster.scenario.bomboperation.service.BombOperationPlayerStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BombOperationPlayerStateServiceImpl implements BombOperationPlayerStateService {

    private static final Logger logger = LoggerFactory.getLogger(BombOperationPlayerStateServiceImpl.class);

    @Autowired
    private BombOperationPlayerStateRepository playerStateRepository;

    @Autowired
    private BombOperationSessionRepository sessionRepository;

    @Autowired
    private BombOperationScoreRepository scoreRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public BombOperationPlayerState createOrUpdatePlayerState(Long sessionId, Long userId, BombOperationTeam team, Boolean hasDefuseKit) {
        logger.info("Création ou mise à jour de l'état du joueur ID: {} pour la session ID: {}", userId, sessionId);

        BombOperationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> {
                    logger.error("Session d'Opération Bombe non trouvée avec l'ID: {}", sessionId);
                    return new BombOperationException.SessionNotFoundException(sessionId);
                });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("Utilisateur non trouvé avec l'ID: {}", userId);
                    return new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId);
                });

        Optional<BombOperationPlayerState> existingState = playerStateRepository.findByBombOperationSessionIdAndUserId(sessionId, userId);

        BombOperationPlayerState playerState;
        if (existingState.isPresent()) {
            playerState = existingState.get();
            logger.info("État du joueur existant trouvé, mise à jour");
        } else {
            playerState = new BombOperationPlayerState();
            playerState.setBombOperationSession(session);
            playerState.setUser(user);
            playerState.setIsAlive(true);
            logger.info("Création d'un nouvel état de joueur");
        }

        if (team != null) {
            playerState.setTeam(team);
        }

        if (hasDefuseKit != null) {
            playerState.setHasDefuseKit(hasDefuseKit);
        }

        playerState = playerStateRepository.save(playerState);
        logger.info("État du joueur créé/mis à jour avec l'ID: {}", playerState.getId());

        // Créer ou mettre à jour le score du joueur
        createOrUpdatePlayerScore(session, user, team);

        return playerState;
    }

    @Override
    public BombOperationPlayerState getPlayerState(Long sessionId, Long userId) {
        logger.info("Récupération de l'état du joueur ID: {} pour la session ID: {}", userId, sessionId);
        return playerStateRepository.findByBombOperationSessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> {
                    logger.error("État du joueur non trouvé pour la session {} et l'utilisateur {}", sessionId, userId);
                    return new BombOperationException.PlayerStateNotFoundException(sessionId, userId);
                });
    }

    @Override
    public List<BombOperationPlayerState> getAllPlayerStates(Long sessionId) {
        logger.info("Récupération de tous les états des joueurs pour la session ID: {}", sessionId);
        return playerStateRepository.findByBombOperationSessionId(sessionId);
    }

    @Override
    public List<BombOperationPlayerState> getPlayerStatesByTeam(Long sessionId, BombOperationTeam team) {
        logger.info("Récupération des états des joueurs de l'équipe {} pour la session ID: {}", team, sessionId);
        return playerStateRepository.findByBombOperationSessionIdAndTeam(sessionId, team);
    }

    @Override
    @Transactional
    public BombOperationPlayerState killPlayer(Long sessionId, Long userId) {
        logger.info("Marquage du joueur ID: {} comme mort pour la session ID: {}", userId, sessionId);

        BombOperationPlayerState playerState = getPlayerState(sessionId, userId);
        playerState.setIsAlive(false);

        playerState = playerStateRepository.save(playerState);
        logger.info("Joueur ID: {} marqué comme mort", userId);

        return playerState;
    }

    @Override
    @Transactional
    public BombOperationPlayerState revivePlayer(Long sessionId, Long userId) {
        logger.info("Marquage du joueur ID: {} comme vivant pour la session ID: {}", userId, sessionId);

        BombOperationPlayerState playerState = getPlayerState(sessionId, userId);
        playerState.setIsAlive(true);

        playerState = playerStateRepository.save(playerState);
        logger.info("Joueur ID: {} marqué comme vivant", userId);

        return playerState;
    }

    @Override
    @Transactional
    public BombOperationPlayerState giveDefuseKit(Long sessionId, Long userId) {
        logger.info("Attribution d'un kit de désamorçage au joueur ID: {} pour la session ID: {}", userId, sessionId);

        BombOperationPlayerState playerState = getPlayerState(sessionId, userId);

        // Vérifier que le joueur est dans l'équipe de défense
        if (playerState.getTeam() != BombOperationTeam.DEFENSE) {
            logger.error("Impossible de donner un kit de désamorçage à un joueur qui n'est pas dans l'équipe de défense");
            throw new BombOperationException.InvalidTeamException(
                    playerState.getTeam().toString(),
                    BombOperationTeam.DEFENSE.toString());
        }

        playerState.setHasDefuseKit(true);

        playerState = playerStateRepository.save(playerState);
        logger.info("Kit de désamorçage attribué au joueur ID: {}", userId);

        return playerState;
    }

    @Override
    @Transactional
    public BombOperationPlayerState removeDefuseKit(Long sessionId, Long userId) {
        logger.info("Retrait du kit de désamorçage au joueur ID: {} pour la session ID: {}", userId, sessionId);

        BombOperationPlayerState playerState = getPlayerState(sessionId, userId);
        playerState.setHasDefuseKit(false);

        playerState = playerStateRepository.save(playerState);
        logger.info("Kit de désamorçage retiré au joueur ID: {}", userId);

        return playerState;
    }

    @Override
    @Transactional
    public BombOperationPlayerState incrementBombsPlanted(Long sessionId, Long userId) {
        logger.info("Incrémentation du compteur de bombes posées pour le joueur ID: {} dans la session ID: {}", userId, sessionId);

        BombOperationPlayerState playerState = getPlayerState(sessionId, userId);
        BombOperationSession session = playerState.getBombOperationSession();

        // Mettre à jour le score du joueur
        Optional<BombOperationScore> scoreOpt = scoreRepository.findByBombOperationScenarioIdAndUserIdAndGameSessionId(
                session.getBombOperationScenario().getId(), userId, session.getGameSessionId());

        if (scoreOpt.isPresent()) {
            BombOperationScore score = scoreOpt.get();
            score.setBombsPlanted(score.getBombsPlanted() + 1);
            scoreRepository.save(score);
            logger.info("Score mis à jour pour le joueur ID: {}, bombes posées: {}", userId, score.getBombsPlanted());
        } else {
            logger.warn("Aucun score trouvé pour le joueur ID: {} dans la session ID: {}", userId, sessionId);
        }

        return playerState;
    }

    @Override
    @Transactional
    public BombOperationPlayerState incrementBombsDefused(Long sessionId, Long userId) {
        logger.info("Incrémentation du compteur de bombes désamorcées pour le joueur ID: {} dans la session ID: {}", userId, sessionId);

        BombOperationPlayerState playerState = getPlayerState(sessionId, userId);
        BombOperationSession session = playerState.getBombOperationSession();

        // Mettre à jour le score du joueur
        Optional<BombOperationScore> scoreOpt = scoreRepository.findByBombOperationScenarioIdAndUserIdAndGameSessionId(
                session.getBombOperationScenario().getId(), userId, session.getGameSessionId());

        if (scoreOpt.isPresent()) {
            BombOperationScore score = scoreOpt.get();
            score.setBombsDefused(score.getBombsDefused() + 1);
            scoreRepository.save(score);
            logger.info("Score mis à jour pour le joueur ID: {}, bombes désamorcées: {}", userId, score.getBombsDefused());
        } else {
            logger.warn("Aucun score trouvé pour le joueur ID: {} dans la session ID: {}", userId, sessionId);
        }

        return playerState;
    }

    @Override
    @Transactional
    public List<BombOperationPlayerState> resetAllPlayerStates(Long sessionId) {
        logger.info("Réinitialisation de tous les états des joueurs pour la session ID: {}", sessionId);

        List<BombOperationPlayerState> playerStates = getAllPlayerStates(sessionId);

        for (BombOperationPlayerState playerState : playerStates) {
            playerState.setIsAlive(true);
            playerStateRepository.save(playerState);
        }

        logger.info("{} états de joueurs réinitialisés pour la session ID: {}", playerStates.size(), sessionId);
        return playerStates;
    }

    @Override
    @Transactional
    public void deletePlayerState(Long sessionId, Long userId) {
        logger.info("Suppression de l'état du joueur ID: {} pour la session ID: {}", userId, sessionId);

        BombOperationPlayerState playerState = getPlayerState(sessionId, userId);
        playerStateRepository.delete(playerState);
        logger.info("État du joueur supprimé pour le joueur ID: {} dans la session ID: {}", userId, sessionId);
    }

    @Override
    @Transactional
    public void deleteAllPlayerStates(Long sessionId) {
        logger.info("Suppression de tous les états des joueurs pour la session ID: {}", sessionId);

        List<BombOperationPlayerState> playerStates = getAllPlayerStates(sessionId);
        playerStateRepository.deleteAll(playerStates);
        logger.info("{} états de joueurs supprimés pour la session ID: {}", playerStates.size(), sessionId);
    }

    /**
     * Crée ou met à jour le score d'un joueur pour une session
     * @param session Session d'Opération Bombe
     * @param user Utilisateur
     * @param team Équipe du joueur
     * @return Le score créé ou mis à jour
     */
    private BombOperationScore createOrUpdatePlayerScore(BombOperationSession session, User user, BombOperationTeam team) {
        logger.info("Création ou mise à jour du score du joueur ID: {} pour la session ID: {}", user.getId(), session.getId());

        Optional<BombOperationScore> existingScore = scoreRepository.findByBombOperationScenarioIdAndUserIdAndGameSessionId(
                session.getBombOperationScenario().getId(), user.getId(), session.getGameSessionId());

        BombOperationScore score;
        if (existingScore.isPresent()) {
            score = existingScore.get();
            logger.info("Score existant trouvé, mise à jour");
        } else {
            score = new BombOperationScore();
            score.setBombOperationScenario(session.getBombOperationScenario());
            score.setUser(user);
            score.setGameSessionId(session.getGameSessionId());
            score.setRoundsWon(0);
            score.setBombsPlanted(0);
            score.setBombsDefused(0);
            logger.info("Création d'un nouveau score");
        }

        score = scoreRepository.save(score);
        logger.info("Score créé/mis à jour avec l'ID: {}", score.getId());

        return score;
    }
}
