package com.airsoft.gamemapmaster.scenario.bomboperation.controller;

import com.airsoft.gamemapmaster.scenario.bomboperation.dto.BombOperationPlayerStateDto;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombOperationPlayerState;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombOperationTeam;
import com.airsoft.gamemapmaster.scenario.bomboperation.service.BombOperationPlayerStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/player-states/bomb-operation")
public class BombOperationPlayerStateController {

    private static final Logger logger = LoggerFactory.getLogger(BombOperationPlayerStateController.class);

    @Autowired
    private BombOperationPlayerStateService playerStateService;

    @PostMapping
    public ResponseEntity<BombOperationPlayerStateDto> createOrUpdatePlayerState(
            @RequestParam Long sessionId,
            @RequestParam Long userId,
            @RequestParam(required = false) String team,
            @RequestParam(required = false) Boolean hasDefuseKit) {

        logger.info("Création ou mise à jour de l'état du joueur ID: {} pour la session ID: {}", userId, sessionId);

        BombOperationTeam bombTeam = null;
        if (team != null) {
            try {
                bombTeam = BombOperationTeam.valueOf(team.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.error("Équipe invalide: {}", team);
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }

        BombOperationPlayerState playerState = playerStateService.createOrUpdatePlayerState(
                sessionId, userId, bombTeam, hasDefuseKit);

        return new ResponseEntity<>(convertToDto(playerState), HttpStatus.CREATED);
    }

    @GetMapping("/{sessionId}/{userId}")
    public ResponseEntity<BombOperationPlayerStateDto> getPlayerState(
            @PathVariable Long sessionId,
            @PathVariable Long userId) {

        logger.info("Récupération de l'état du joueur ID: {} pour la session ID: {}", userId, sessionId);

        BombOperationPlayerState playerState = playerStateService.getPlayerState(sessionId, userId);

        return new ResponseEntity<>(convertToDto(playerState), HttpStatus.OK);
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<BombOperationPlayerStateDto>> getAllPlayerStates(@PathVariable Long sessionId) {
        logger.info("Récupération de tous les états des joueurs pour la session ID: {}", sessionId);

        List<BombOperationPlayerState> playerStates = playerStateService.getAllPlayerStates(sessionId);

        List<BombOperationPlayerStateDto> playerStateDtos = playerStates.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new ResponseEntity<>(playerStateDtos, HttpStatus.OK);
    }

    @GetMapping("/session/{sessionId}/team/{team}")
    public ResponseEntity<List<BombOperationPlayerStateDto>> getPlayerStatesByTeam(
            @PathVariable Long sessionId,
            @PathVariable String team) {

        logger.info("Récupération des états des joueurs de l'équipe {} pour la session ID: {}", team, sessionId);

        BombOperationTeam bombTeam;
        try {
            bombTeam = BombOperationTeam.valueOf(team.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.error("Équipe invalide: {}", team);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        List<BombOperationPlayerState> playerStates = playerStateService.getPlayerStatesByTeam(sessionId, bombTeam);

        List<BombOperationPlayerStateDto> playerStateDtos = playerStates.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new ResponseEntity<>(playerStateDtos, HttpStatus.OK);
    }

    @PostMapping("/{sessionId}/{userId}/kill")
    public ResponseEntity<BombOperationPlayerStateDto> killPlayer(
            @PathVariable Long sessionId,
            @PathVariable Long userId) {

        logger.info("Marquage du joueur ID: {} comme mort pour la session ID: {}", userId, sessionId);

        BombOperationPlayerState playerState = playerStateService.killPlayer(sessionId, userId);

        return new ResponseEntity<>(convertToDto(playerState), HttpStatus.OK);
    }

    @PostMapping("/{sessionId}/{userId}/revive")
    public ResponseEntity<BombOperationPlayerStateDto> revivePlayer(
            @PathVariable Long sessionId,
            @PathVariable Long userId) {

        logger.info("Marquage du joueur ID: {} comme vivant pour la session ID: {}", userId, sessionId);

        BombOperationPlayerState playerState = playerStateService.revivePlayer(sessionId, userId);

        return new ResponseEntity<>(convertToDto(playerState), HttpStatus.OK);
    }

    @PostMapping("/{sessionId}/{userId}/give-defuse-kit")
    public ResponseEntity<BombOperationPlayerStateDto> giveDefuseKit(
            @PathVariable Long sessionId,
            @PathVariable Long userId) {

        logger.info("Attribution d'un kit de désamorçage au joueur ID: {} pour la session ID: {}", userId, sessionId);

        BombOperationPlayerState playerState = playerStateService.giveDefuseKit(sessionId, userId);

        return new ResponseEntity<>(convertToDto(playerState), HttpStatus.OK);
    }

    @PostMapping("/{sessionId}/{userId}/remove-defuse-kit")
    public ResponseEntity<BombOperationPlayerStateDto> removeDefuseKit(
            @PathVariable Long sessionId,
            @PathVariable Long userId) {

        logger.info("Retrait du kit de désamorçage au joueur ID: {} pour la session ID: {}", userId, sessionId);

        BombOperationPlayerState playerState = playerStateService.removeDefuseKit(sessionId, userId);

        return new ResponseEntity<>(convertToDto(playerState), HttpStatus.OK);
    }

    @PostMapping("/{sessionId}/{userId}/increment-bombs-planted")
    public ResponseEntity<BombOperationPlayerStateDto> incrementBombsPlanted(
            @PathVariable Long sessionId,
            @PathVariable Long userId) {

        logger.info("Incrémentation du compteur de bombes posées pour le joueur ID: {} dans la session ID: {}",
                userId, sessionId);

        BombOperationPlayerState playerState = playerStateService.incrementBombsPlanted(sessionId, userId);

        return new ResponseEntity<>(convertToDto(playerState), HttpStatus.OK);
    }

    @PostMapping("/{sessionId}/{userId}/increment-bombs-defused")
    public ResponseEntity<BombOperationPlayerStateDto> incrementBombsDefused(
            @PathVariable Long sessionId,
            @PathVariable Long userId) {

        logger.info("Incrémentation du compteur de bombes désamorcées pour le joueur ID: {} dans la session ID: {}",
                userId, sessionId);

        BombOperationPlayerState playerState = playerStateService.incrementBombsDefused(sessionId, userId);

        return new ResponseEntity<>(convertToDto(playerState), HttpStatus.OK);
    }

    @PostMapping("/{sessionId}/reset-all")
    public ResponseEntity<List<BombOperationPlayerStateDto>> resetAllPlayerStates(@PathVariable Long sessionId) {
        logger.info("Réinitialisation de tous les états des joueurs pour la session ID: {}", sessionId);

        List<BombOperationPlayerState> playerStates = playerStateService.resetAllPlayerStates(sessionId);

        List<BombOperationPlayerStateDto> playerStateDtos = playerStates.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new ResponseEntity<>(playerStateDtos, HttpStatus.OK);
    }

    @DeleteMapping("/{sessionId}/{userId}")
    public ResponseEntity<Void> deletePlayerState(
            @PathVariable Long sessionId,
            @PathVariable Long userId) {

        logger.info("Suppression de l'état du joueur ID: {} pour la session ID: {}", userId, sessionId);

        playerStateService.deletePlayerState(sessionId, userId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<Void> deleteAllPlayerStates(@PathVariable Long sessionId) {
        logger.info("Suppression de tous les états des joueurs pour la session ID: {}", sessionId);

        playerStateService.deleteAllPlayerStates(sessionId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private BombOperationPlayerStateDto convertToDto(BombOperationPlayerState playerState) {
        BombOperationPlayerStateDto dto = new BombOperationPlayerStateDto();
        dto.setId(playerState.getId());
        dto.setSessionId(playerState.getBombOperationSession().getId());
        dto.setUserId(playerState.getUser().getId());
        dto.setUsername(playerState.getUser().getUsername());
        dto.setTeam(playerState.getTeam().toString());
        dto.setIsAlive(playerState.getIsAlive());
        dto.setHasDefuseKit(playerState.getHasDefuseKit());
        return dto;
    }
}
