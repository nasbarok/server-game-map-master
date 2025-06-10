package com.airsoft.gamemapmaster.scenario.bomboperation.controller;

import com.airsoft.gamemapmaster.scenario.bomboperation.dto.BombArmedRequestDto;
import com.airsoft.gamemapmaster.scenario.bomboperation.dto.BombDisarmedRequestDto;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/game-sessions/bomb-operation")
public class BombOperationSessionController {

    private static final Logger logger = LoggerFactory.getLogger(BombOperationSessionController.class);

    @Autowired
    private BombOperationSessionService bombOperationSessionService;

    @PostMapping
    public ResponseEntity<BombOperationSessionDto> createSession(
            @RequestParam Long scenarioId,
            @RequestParam Long gameSessionId) {

        logger.info("Création d'une nouvelle session pour le scénario d'Opération Bombe ID: {} et la session de jeu ID: {}",
                scenarioId, gameSessionId);

        BombOperationSessionDto bombOperationSessionDto= bombOperationSessionService.createBombOperationSession(scenarioId, gameSessionId);
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
    @PostMapping("/{sessionId}/bomb-armed")
    public ResponseEntity<BombOperationSessionDto> bombArmed(
            @PathVariable Long sessionId,
            @RequestBody BombArmedRequestDto request) {

        logger.info("Notification d'armement de bombe terminé pour la session ID: {}, utilisateur ID: {}, site ID: {}",
                sessionId, request.getUserId(), request.getSiteId());

        BombOperationSession session = bombOperationSessionService.bombArmed(
                sessionId,
                request.getUserId(),
                request.getSiteId(),
                request.getLatitude(),
                request.getLongitude()
        );

        return new ResponseEntity<>(session.toDto(null), HttpStatus.OK);
    }


    /**
     * Endpoint pour notifier qu'une bombe a été désarmée (gestion côté Flutter terminée)
     */
    @PostMapping("/{sessionId}/bomb-disarmed")
    public ResponseEntity<BombOperationSessionDto> bombDisarmed(
            @PathVariable Long sessionId,
            @RequestBody BombDisarmedRequestDto request) {

        logger.info("Notification de désarmement de bombe terminé pour la session ID: {}, utilisateur ID: {}, site ID: {}",
                sessionId, request.getUserId(), request.getSiteId());

        BombOperationSession session = bombOperationSessionService.bombDisarmed(
                sessionId,
                request.getUserId(),
                request.getSiteId(),
                request.getLatitude(),
                request.getLongitude()
        );

        return new ResponseEntity<>(session.toDto(null), HttpStatus.OK);
    }
}
