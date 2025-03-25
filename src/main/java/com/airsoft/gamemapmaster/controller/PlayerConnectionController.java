package com.airsoft.gamemapmaster.controller;

import com.airsoft.gamemapmaster.model.ConnectedPlayer;
import com.airsoft.gamemapmaster.model.GameMap;
import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.service.ConnectedPlayerService;
import com.airsoft.gamemapmaster.service.GameMapService;
import com.airsoft.gamemapmaster.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/maps")
public class PlayerConnectionController {
    private static final Logger logger = LoggerFactory.getLogger(PlayerConnectionController.class);

    @Autowired
    private ConnectedPlayerService connectedPlayerService;

    @Autowired
    private UserService userService;

    @Autowired
    private GameMapService gameMapService;

    /**
     * Endpoint pour qu'un joueur rejoigne une carte
     */
    @PostMapping("/{mapId}/join")
    public ResponseEntity<?> joinMap(@PathVariable("mapId") Long mapId,
                                     @RequestParam(value = "teamId", required = false) Long teamId,
                                     Authentication authentication) {
        String username = authentication.getName();
        Optional<User> user = userService.findByUsername(username);

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }

        // Vérifier si la carte existe
        if (gameMapService.findById(mapId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Carte non trouvée");
        }

        // Vérifier si le joueur est déjà connecté à cette carte
        if (connectedPlayerService.isPlayerConnectedToMap(mapId, user.get().getId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Vous êtes déjà connecté à cette carte");
        }

        ConnectedPlayer connectedPlayer = connectedPlayerService.connectPlayerToMap(mapId, user.get().getId(), teamId);

        if (connectedPlayer == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Impossible de rejoindre la carte");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(connectedPlayer);
    }

    /**
     * Endpoint pour qu'un joueur quitte une carte
     */
    @PostMapping("/{mapId}/leave")
    public ResponseEntity<?> leaveMap(@PathVariable("mapId") Long mapId, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> user = userService.findByUsername(username);

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }

        boolean disconnected = connectedPlayerService.disconnectPlayerFromMap(mapId, user.get().getId());

        if (!disconnected) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vous n'êtes pas connecté à cette carte");
        }

        return ResponseEntity.ok(Map.of("message", "Vous avez quitté la carte avec succès"));
    }

    /**
     * Endpoint pour lister tous les joueurs connectés à une carte
     */
    @GetMapping("/{mapId}/players")
    public ResponseEntity<?> getConnectedPlayers(@PathVariable("mapId") Long mapId) {
        // Vérifier si la carte existe
        if (gameMapService.findById(mapId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Carte non trouvée");
        }

        List<ConnectedPlayer> connectedPlayers = connectedPlayerService.getConnectedPlayersByMapId(mapId);

        return ResponseEntity.ok(connectedPlayers);
    }

    /**
     * Endpoint pour déconnecter tous les joueurs d'une carte (réservé au propriétaire de la carte)
     */
    @PostMapping("/{mapId}/close")
    public ResponseEntity<?> closeMap(@PathVariable("mapId") Long mapId, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> user = userService.findByUsername(username);

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }

        // Vérifier si l'utilisateur est le propriétaire de la carte
        return gameMapService.findById(mapId)
                .map(gameMap -> {
                    if (!gameMap.getOwner().getId().equals(user.get().getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body("Vous n'êtes pas autorisé à fermer cette carte");
                    }

                    int disconnectedCount = connectedPlayerService.disconnectAllPlayersFromMap(mapId);

                    return ResponseEntity.ok(Map.of(
                            "message", "Carte fermée avec succès",
                            "disconnectedPlayers", disconnectedCount
                    ));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Carte non trouvée"));
    }

    /**
     * Endpoint pour assigner un joueur à une équipe
     */
    @PostMapping("/{mapId}/players/{userId}/team/{teamId}")
    public ResponseEntity<?> assignPlayerToTeam(@PathVariable("mapId") Long mapId,
                                                @PathVariable("userId") Long userId,
                                                @PathVariable("teamId") Long teamId,
                                                Authentication authentication) {
        String username = authentication.getName();
        Optional<User> currentUser = userService.findByUsername(username);

        if (currentUser.isEmpty()) {
            logger.warn("Tentative d'assignation d'un joueur à une équipe par un utilisateur non trouvé : {}", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }

        Long ownerId = currentUser.get().getId();
        logger.info("Utilisateur {} (ID: {}) tente d'assigner l'utilisateur {} à l'équipe {} sur la carte {}",
                username, ownerId, userId, teamId, mapId);

        Optional<GameMap> optionalMap = gameMapService.findById(mapId);

        if (optionalMap.isEmpty()) {
            logger.warn("Carte {} introuvable pour l'assignation du joueur {} à l'équipe {}", mapId, userId, teamId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Carte non trouvée");
        }

        GameMap gameMap = optionalMap.get();

        if (!gameMap.getOwner().getId().equals(ownerId)) {
            logger.warn("Utilisateur {} (ID: {}) n'est pas propriétaire de la carte {} et ne peut pas assigner de joueurs",
                    username, ownerId, mapId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Vous n'êtes pas autorisé à assigner des joueurs à des équipes");
        }

        Optional<ConnectedPlayer> assignment = connectedPlayerService.assignPlayerToTeam(mapId, userId, teamId);

        if (assignment.isPresent()) {
            logger.info("Joueur {} assigné avec succès à l'équipe {} sur la carte {}", userId, teamId, mapId);
            return ResponseEntity.ok(assignment.get());
        } else {
            logger.warn("Échec de l'assignation du joueur {} à l'équipe {} sur la carte {} : joueur ou équipe introuvable",
                    userId, teamId, mapId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Joueur ou équipe non trouvé");
        }
    }

}
