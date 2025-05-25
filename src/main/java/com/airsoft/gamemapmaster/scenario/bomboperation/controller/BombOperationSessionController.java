package com.airsoft.gamemapmaster.scenario.bomboperation.controller;

import com.airsoft.gamemapmaster.scenario.bomboperation.dto.BombOperationSessionDto;
import com.airsoft.gamemapmaster.scenario.bomboperation.dto.BombSiteDto;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombOperationSession;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombSite;
import com.airsoft.gamemapmaster.scenario.bomboperation.service.BombOperationSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sessions/bomb-operation")
public class BombOperationSessionController {

    private static final Logger logger = LoggerFactory.getLogger(BombOperationSessionController.class);

    @Autowired
    private BombOperationSessionService sessionService;

    @PostMapping
    public ResponseEntity<BombOperationSessionDto> createSession(
            @RequestParam Long scenarioId,
            @RequestParam Long gameSessionId) {

        logger.info("Création d'une nouvelle session pour le scénario d'Opération Bombe ID: {} et la session de jeu ID: {}",
                scenarioId, gameSessionId);

        BombOperationSession session = sessionService.createSession(scenarioId, gameSessionId);

        return new ResponseEntity<>(convertToDto(session), HttpStatus.CREATED);
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<BombOperationSessionDto> getSessionById(@PathVariable Long sessionId) {
        logger.info("Récupération de la session d'Opération Bombe ID: {}", sessionId);

        BombOperationSession session = sessionService.getSessionById(sessionId);

        return new ResponseEntity<>(convertToDto(session), HttpStatus.OK);
    }

    @GetMapping("/by-game-session/{gameSessionId}")
    public ResponseEntity<BombOperationSessionDto> getSessionByGameSessionId(@PathVariable Long gameSessionId) {
        logger.info("Récupération de la session d'Opération Bombe par session de jeu ID: {}", gameSessionId);

        BombOperationSession session = sessionService.getSessionByGameSessionId(gameSessionId);

        return new ResponseEntity<>(convertToDto(session), HttpStatus.OK);
    }

    @PostMapping("/{sessionId}/plant-bomb")
    public ResponseEntity<BombOperationSessionDto> plantBomb(
            @PathVariable Long sessionId,
            @RequestParam Long userId,
            @RequestParam Long siteId,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {

        logger.info("Tentative de pose de bombe par l'utilisateur ID: {} sur le site ID: {} pour la session ID: {}",
                userId, siteId, sessionId);

        BombOperationSession session = sessionService.plantBomb(sessionId, userId, siteId, latitude, longitude);

        return new ResponseEntity<>(convertToDto(session), HttpStatus.OK);
    }

    @PostMapping("/{sessionId}/start-defusing")
    public ResponseEntity<BombOperationSessionDto> startDefusing(
            @PathVariable Long sessionId,
            @RequestParam Long userId,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {

        logger.info("Tentative de désamorçage de bombe par l'utilisateur ID: {} pour la session ID: {}",
                userId, sessionId);

        BombOperationSession session = sessionService.startDefusing(sessionId, userId, latitude, longitude);

        return new ResponseEntity<>(convertToDto(session), HttpStatus.OK);
    }

    @PostMapping("/{sessionId}/finish-defusing")
    public ResponseEntity<BombOperationSessionDto> finishDefusing(
            @PathVariable Long sessionId,
            @RequestParam Long userId) {

        logger.info("Fin du désamorçage de bombe par l'utilisateur ID: {} pour la session ID: {}",
                userId, sessionId);

        BombOperationSession session = sessionService.finishDefusing(sessionId, userId);

        return new ResponseEntity<>(convertToDto(session), HttpStatus.OK);
    }

    @PostMapping("/{sessionId}/explode-bomb")
    public ResponseEntity<BombOperationSessionDto> explodeBomb(@PathVariable Long sessionId) {
        logger.info("Explosion de la bombe pour la session ID: {}", sessionId);

        BombOperationSession session = sessionService.explodeBomb(sessionId);

        return new ResponseEntity<>(convertToDto(session), HttpStatus.OK);
    }

    @PostMapping("/{sessionId}/end-game")
    public ResponseEntity<BombOperationSessionDto> endGame(@PathVariable Long sessionId) {
        logger.info("Fin de la partie pour la session ID: {}", sessionId);

        BombOperationSession session = sessionService.endGame(sessionId);

        return new ResponseEntity<>(convertToDto(session), HttpStatus.OK);
    }

    @GetMapping("/{sessionId}/is-player-in-active-bomb-site")
    public ResponseEntity<?> isPlayerInActiveBombSite(
            @PathVariable Long sessionId,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {

        logger.info("Vérification si le joueur est dans un site de bombe actif pour la session ID: {}", sessionId);

        BombSite bombSite = sessionService.isPlayerInActiveBombSite(sessionId, latitude, longitude);

        if (bombSite == null) {
            return new ResponseEntity<>(false, HttpStatus.OK);
        }

        return new ResponseEntity<>(convertToDto(bombSite), HttpStatus.OK);
    }

    @GetMapping("/{sessionId}/active-bomb-sites")
    public ResponseEntity<List<BombSiteDto>> getActiveBombSites(@PathVariable Long sessionId) {
        logger.info("Récupération des sites de bombe actifs pour la session ID: {}", sessionId);

        List<BombSite> activeSites = sessionService.getActiveBombSites(sessionId);

        List<BombSiteDto> activeSiteDtos = activeSites.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new ResponseEntity<>(activeSiteDtos, HttpStatus.OK);
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long sessionId) {
        logger.info("Suppression de la session d'Opération Bombe ID: {}", sessionId);

        sessionService.deleteSession(sessionId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private BombOperationSessionDto convertToDto(BombOperationSession session) {
        BombOperationSessionDto dto = new BombOperationSessionDto();
        dto.setId(session.getId());
        dto.setBombOperationScenarioId(session.getBombOperationScenario().getId());
        dto.setGameSessionId(session.getGameSessionId());
        dto.setCurrentRound(session.getCurrentRound());
        dto.setAttackTeamScore(session.getAttackTeamScore());
        dto.setDefenseTeamScore(session.getDefenseTeamScore());
        dto.setGameState(session.getGameState().toString());
        dto.setRoundStartTime(session.getRoundStartTime());
        dto.setBombPlantedTime(session.getBombPlantedTime());
        dto.setDefuseStartTime(session.getDefuseStartTime());
        dto.setActiveBombSiteIds(session.getActiveBombSiteIds());
        return dto;
    }

    private BombSiteDto convertToDto(BombSite bombSite) {
        BombSiteDto dto = new BombSiteDto();
        dto.setId(bombSite.getId());
        dto.setName(bombSite.getName());
        dto.setLatitude(bombSite.getLatitude());
        dto.setLongitude(bombSite.getLongitude());
        dto.setRadius(bombSite.getRadius());
        dto.setBombOperationScenarioId(bombSite.getBombOperationScenario().getId());
        return dto;
    }
}
