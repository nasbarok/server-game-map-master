package com.airsoft.gamemapmaster.scenario.bomboperation.controller;

import com.airsoft.gamemapmaster.scenario.bomboperation.dto.BombArmedRequestDto;
import com.airsoft.gamemapmaster.scenario.bomboperation.dto.BombDisarmedRequestDto;
import com.airsoft.gamemapmaster.scenario.bomboperation.dto.BombOperationSessionDto;
import com.airsoft.gamemapmaster.scenario.bomboperation.dto.BombSiteDto;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombOperationScenario;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombOperationSession;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombSite;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombSiteSessionState;
import com.airsoft.gamemapmaster.scenario.bomboperation.service.BombOperationPlayerStateService;
import com.airsoft.gamemapmaster.scenario.bomboperation.service.BombOperationSessionService;
import com.airsoft.gamemapmaster.scenario.bomboperation.service.BombSiteSessionStateService;
import com.airsoft.gamemapmaster.scenario.bomboperation.websocket.BombOperationWebSocketNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/game-sessions/bomb-operation")
public class BombOperationSessionController {

    private static final Logger logger = LoggerFactory.getLogger(BombOperationSessionController.class);

    @Autowired
    private BombOperationSessionService bombOperationSessionService;
    @Autowired
    private BombSiteSessionStateService bombSiteSessionStateService;

    @Autowired
    private BombOperationPlayerStateService bombOperationPlayerStateService;

    @Autowired
    private BombOperationWebSocketNotifier bombOperationWebSocketNotifier;

    @PostMapping
    public ResponseEntity<BombOperationSessionDto> createSession(
            @RequestParam Long scenarioId,
            @RequestParam Long gameSessionId) {

        logger.info("Création d'une nouvelle session pour le scénario d'Opération Bombe ID: {} et la session de jeu ID: {}",
                scenarioId, gameSessionId);

        BombOperationSessionDto bombOperationSessionDto = bombOperationSessionService.createBombOperationSession(scenarioId, gameSessionId);
        logger.info("🎯 Sites à activer: {}", bombOperationSessionDto.getToActiveBombSites());
        logger.info("🎯 Sites désactivés: {}", bombOperationSessionDto.getDisableBombSites());
        logger.info("🎯 Sites actifs: {}", bombOperationSessionDto.getActiveBombSites());
        return new ResponseEntity<>(bombOperationSessionDto, HttpStatus.CREATED);
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<BombOperationSessionDto> getSessionById(@PathVariable Long sessionId) {
        logger.info("Récupération de la session d'Opération Bombe ID: {}", sessionId);

        BombOperationSession session = bombOperationSessionService.getSessionById(sessionId);

        return new ResponseEntity<>(session.toDto(null), HttpStatus.OK);
    }

    @GetMapping("/by-game-session/{gameSessionId}")
    public ResponseEntity<BombOperationSessionDto> getSessionByGameSessionId(@PathVariable Long gameSessionId) {
        logger.info("Récupération de la session d'Opération Bombe par session de jeu ID: {}", gameSessionId);

        BombOperationSessionDto bombOperationSessionDto = this.bombOperationSessionService.getBombOperationSessionDtoByGameSessionId(gameSessionId);


        return new ResponseEntity<>(bombOperationSessionDto, HttpStatus.OK);
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

        BombOperationSession session = bombOperationSessionService.plantBomb(sessionId, userId, siteId, latitude, longitude);

        return new ResponseEntity<>(session.toDto(null), HttpStatus.OK);
    }

    @PostMapping("/{sessionId}/start-defusing")
    public ResponseEntity<BombOperationSessionDto> startDefusing(
            @PathVariable Long sessionId,
            @RequestParam Long userId,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {

        logger.info("Tentative de désamorçage de bombe par l'utilisateur ID: {} pour la session ID: {}",
                userId, sessionId);

        BombOperationSession session = bombOperationSessionService.startDefusing(sessionId, userId, latitude, longitude);

        return new ResponseEntity<>(session.toDto(null), HttpStatus.OK);
    }

    @PostMapping("/{sessionId}/finish-defusing")
    public ResponseEntity<BombOperationSessionDto> finishDefusing(
            @PathVariable Long sessionId,
            @RequestParam Long userId) {

        logger.info("Fin du désamorçage de bombe par l'utilisateur ID: {} pour la session ID: {}",
                userId, sessionId);

        BombOperationSession session = bombOperationSessionService.finishDefusing(sessionId, userId);

        return new ResponseEntity<>(session.toDto(null), HttpStatus.OK);
    }

    @PostMapping("/{sessionId}/explode-bomb")
    public ResponseEntity<BombOperationSessionDto> explodeBomb(@PathVariable Long sessionId) {
        logger.info("Explosion de la bombe pour la session ID: {}", sessionId);

        BombOperationSession session = bombOperationSessionService.explodeBomb(sessionId);

        return new ResponseEntity<>(session.toDto(null), HttpStatus.OK);
    }

    @PostMapping("/{sessionId}/end-game")
    public ResponseEntity<BombOperationSessionDto> endGame(@PathVariable Long sessionId) {
        logger.info("Fin de la partie pour la session ID: {}", sessionId);

        BombOperationSession session = bombOperationSessionService.endGame(sessionId);

        return new ResponseEntity<>(session.toDto(null), HttpStatus.OK);
    }

    @GetMapping("/{sessionId}/is-player-in-active-bomb-site")
    public ResponseEntity<?> isPlayerInActiveBombSite(
            @PathVariable Long sessionId,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {

        logger.info("Vérification si le joueur est dans un site de bombe actif pour la session ID: {}", sessionId);

        BombSite bombSite = bombOperationSessionService.isPlayerInActiveBombSite(sessionId, latitude, longitude);

        if (bombSite == null) {
            return new ResponseEntity<>(false, HttpStatus.OK);
        }

        return new ResponseEntity<>(bombSite.toDto(), HttpStatus.OK);
    }

    @GetMapping("/{sessionId}/active-bomb-sites")
    public ResponseEntity<List<BombSiteDto>> getActiveBombSites(@PathVariable Long sessionId) {
        logger.info("Récupération des sites de bombe actifs pour la session ID: {}", sessionId);

        List<BombSite> activeSites = bombOperationSessionService.getActiveBombSites(sessionId);

        List<BombSiteDto> activeSiteDtos = new ArrayList<>();
        for (BombSite site : activeSites) {
            activeSiteDtos.add(site.toDto());
        }

        return new ResponseEntity<>(activeSiteDtos, HttpStatus.OK);
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long sessionId) {
        logger.info("Suppression de la session d'Opération Bombe ID: {}", sessionId);

        bombOperationSessionService.deleteSession(sessionId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Sauvegarde les rôles des équipes pour une session de jeu
     */
    @PostMapping("/{gameSessionId}/team-roles")
    public ResponseEntity<Void> saveTeamRoles(
            @PathVariable Long gameSessionId,
            @RequestBody Map<String, String> teamRoles) {

        bombOperationSessionService.saveTeamRoles(gameSessionId, teamRoles);
        return ResponseEntity.ok().build();
    }

    /**
     * Récupère les rôles des équipes pour une session de jeu
     */
    @GetMapping("/{gameSessionId}/team-roles")
    public ResponseEntity<Map<String, String>> getTeamRoles(@PathVariable Long gameSessionId) {
        Map<String, String> teamRoles = bombOperationSessionService.getTeamRoles(gameSessionId);
        return ResponseEntity.ok(teamRoles);
    }

    @PostMapping("/{gameSessionId}/active-bomb-sites")
    public ResponseEntity<List<BombSiteDto>> setActiveBombSites(
            @PathVariable Long gameSessionId) {
        logger.info("🎯 Sélection aléatoire des sites à activer pour la session ID: {}", gameSessionId);

        List<BombSite> selectedBombSites = bombOperationSessionService.selectAndActivateRandomSites(gameSessionId);

        List<BombSiteDto> selectedBombSiteDtos = new ArrayList<>();
        for (BombSite site : selectedBombSites) {
            selectedBombSiteDtos.add(site.toDto());
        }

        return ResponseEntity.ok(selectedBombSiteDtos);
    }


    /**
     * Endpoint pour notifier qu'une bombe a été armée (gestion côté Flutter terminée)
     */
    @PostMapping("/{fieldId}/{gameSessionId}/bomb-armed")
    public ResponseEntity<BombOperationSessionDto> bombArmed(
            @PathVariable Long gameSessionId,
            @PathVariable Long fieldId,
            @RequestBody BombArmedRequestDto request) {

        logger.info("Notification d'armement de bombe terminé pour la gamesession ID: {}, fieldId: {},utilisateur ID: {}, BombSiteId: {}",
                gameSessionId, fieldId, request.getUserId(), request.getBombSiteId());
        //recuperation du BombOperationSession
        BombOperationSession bombOperationSession = bombOperationSessionService.getBombOperationSessionByGameSessionId(gameSessionId);
        BombOperationScenario bombOperationScenario = bombOperationSession.getBombOperationScenario();
        int bombTimer = bombOperationScenario.getBombTimer();
        bombSiteSessionStateService.getActiveSites(gameSessionId);

        // 1. Mettre à jour l'état du site
        BombSiteSessionState siteArmedState = bombSiteSessionStateService.armBomb(
                gameSessionId,
                request.getBombSiteId(),
                request.getUserId(),
                bombTimer
        );
        logger.info("Site armé: {}", siteArmedState.getName());

        // 4. Notifier via WebSocket
        bombOperationWebSocketNotifier.sendBombPlantedNotification(
                fieldId,
                gameSessionId,
                request.getUserId(),
                request.getBombSiteId(),
                siteArmedState.getName(),
                bombTimer
        );
        logger.info("Notification WebSocket envoyée pour l'armement de la bombe: {}",
                siteArmedState.getName());
        return new ResponseEntity<>(bombOperationSession.toDto(null), HttpStatus.OK);
    }


    /**
     * Endpoint pour notifier qu'une bombe a été désarmée (gestion côté Flutter terminée)
     */
    @PostMapping("/{fieldId}/{gameSessionId}/bomb-disarmed")
    public ResponseEntity<BombOperationSessionDto> bombDisarmed(
            @PathVariable Long gameSessionId,
            @PathVariable Long fieldId,
            @RequestBody BombDisarmedRequestDto request) {

        logger.info("Notification de désarmement de bombe terminé pour la session ID: {}, terrain: {}, utilisateur ID: {}, site ID: {}",
                gameSessionId, fieldId, request.getUserId(), request.getBombSiteId());

        // 1. Récupération de la session
        BombOperationSession bombOperationSession = bombOperationSessionService.getBombOperationSessionByGameSessionId(gameSessionId);

        // 2. Mise à jour de l'état du site (désarmement)
        BombSiteSessionState siteDisarmedState = bombSiteSessionStateService.disarmBomb(
                gameSessionId,
                request.getBombSiteId(),
                request.getUserId()
        );
        logger.info("Site désarmé: {}", siteDisarmedState.getName());
        // 4. Notification WebSocket
        bombOperationWebSocketNotifier.sendDefuseSuccessNotification(
                fieldId,
                gameSessionId,
                request.getUserId(),
                request.getBombSiteId(),
                siteDisarmedState.getName()
        );
        logger.info("Notification WebSocket envoyée pour le désarmement de la bombe: {}",
                siteDisarmedState.getName());
        return new ResponseEntity<>(bombOperationSession.toDto(null), HttpStatus.OK);
    }

}
