package com.airsoft.gamemapmaster.controller;

import com.airsoft.gamemapmaster.model.ConnectedPlayer;
import com.airsoft.gamemapmaster.model.GameMap;
import com.airsoft.gamemapmaster.model.Team;
import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.service.*;
import com.airsoft.gamemapmaster.websocket.WebSocketMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fields")
public class PlayerConnectionController {
    private static final Logger logger = LoggerFactory.getLogger(PlayerConnectionController.class);

    @Autowired
    private ConnectedPlayerService connectedPlayerService;

    @Autowired
    private UserService userService;

    @Autowired
    private GameMapService gameMapService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private FieldService fieldService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    /**
     * Endpoint pour qu'un joueur rejoigne une carte
     */
    @PostMapping("/{fieldId}/join")
    public ResponseEntity<?> joinMap(@PathVariable("fieldId") Long fieldId,
                                     @RequestParam(value = "teamId", required = false) Long teamId,
                                     Authentication authentication) {
        String username = authentication.getName();
        Optional<User> user = userService.findByUsername(username);

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }

        // Vérifier si la carte existe
        if (fieldService.findById(fieldId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Carte non trouvée");
        }

        // Vérifier si le joueur est déjà connecté à cette carte
        if (connectedPlayerService.isPlayerConnectedToField(fieldId, user.get().getId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Vous êtes déjà connecté à cette carte");
        }

        ConnectedPlayer connectedPlayer = connectedPlayerService.connectPlayerToField(fieldId, user.get().getId(), teamId);

        if (connectedPlayer == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Impossible de rejoindre la carte");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(connectedPlayer);
    }

    /**
     * Endpoint pour qu'un joueur quitte un terrain
     */
    @PostMapping("/{fieldId}/leave")
    public ResponseEntity<?> leaveMap(@PathVariable("fieldId") Long fieldId, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> user = userService.findByUsername(username);

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }

        boolean disconnected = connectedPlayerService.disconnectPlayerFromField(fieldId, user.get().getId());

        if (!disconnected) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vous n'êtes pas connecté à cette carte");
        }

        return ResponseEntity.ok(Map.of("message", "Vous avez quitté la carte avec succès"));
    }

    /**
     * Endpoint pour lister tous les joueurs connectés à une carte
     */
    @GetMapping("/{fieldId}/players")
    public ResponseEntity<?> getConnectedPlayers(@PathVariable("fieldId") Long fieldId) {
        // Vérifier si la carte existe
        if (fieldService.findById(fieldId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Carte non trouvée");
        }

        List<ConnectedPlayer> connectedPlayers = connectedPlayerService.getConnectedPlayersByFieldId(fieldId);

        return ResponseEntity.ok(connectedPlayers);
    }

    /**
     * Endpoint pour assigner un joueur à une équipe
     */
    @PostMapping("/{mapId}/players/{playerId}/team/{teamId}")
    public ResponseEntity<?> assignPlayerToTeam(@PathVariable("mapId") Long mapId,
                                                @PathVariable("playerId") Long playerId,
                                                @PathVariable("teamId") Long teamId,
                                                Authentication authentication) {
        String username = authentication.getName();
        Optional<User> currentUser = userService.findByUsername(username);

        // 🔐 Vérification utilisateur
        if (currentUser.isEmpty()) {
            logger.warn("❌ Utilisateur non trouvé pour le token JWT : {}", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }

        Long ownerId = currentUser.get().getId();
        logger.info("🧾 Utilisateur '{}' (ID: {}) tente d'assigner le joueur {} à l'équipe {} sur la carte {}",
                username, ownerId, playerId, teamId, mapId);

        // 📍 Vérification que la carte existe
        Optional<GameMap> optionalMap = gameMapService.findById(mapId);
        if (optionalMap.isEmpty()) {
            logger.warn("❌ Carte introuvable : ID={}", mapId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Carte non trouvée");
        }

        GameMap gameMap = optionalMap.get();

        // 🔍 Vérification que le joueur est bien connecté à cette carte
        Optional<ConnectedPlayer> connectedPlayerOpt =
                connectedPlayerService.getConnectedPlayerByUserAndMap(playerId, mapId);

        if (connectedPlayerOpt.isEmpty()) {
            logger.warn("❌ Joueur non connecté à la carte : userId={}, mapId={}", playerId, mapId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Joueur non connecté à cette carte");
        }
        Optional<ConnectedPlayer> updatedPlayer = Optional.empty();
        String teamName = "no_team";

        // 🧪 Vérification que l’équipe existe
        Optional<Team> teamOpt = teamService.findById(teamId);
        if (teamOpt.isEmpty()) {
            //remove the player from the team
            connectedPlayerOpt.get().setTeam(null);
            updatedPlayer = connectedPlayerService.save(connectedPlayerOpt.get());
            logger.info("Joueur {} retiré de l'équipe pour la carte {}", playerId, mapId);
        }else{
            // ✅ Affectation
            ConnectedPlayer player = connectedPlayerOpt.get();
            player.setTeam(teamOpt.get());
            connectedPlayerService.save(player);

            // Après l'assignation réussie
            updatedPlayer = connectedPlayerService.save(player);
            teamName = teamOpt.get().getName();
            // Log détaillé pour le débogage
            logger.info("Joueur {} assigné à l'équipe {} pour la carte {}", playerId, teamId, mapId);
        }

        // Envoyer une notification WebSocket
        WebSocketMessage teamUpdateMessage = new WebSocketMessage(
                "TEAM_UPDATE",
                Map.of(
                        "mapId", mapId,
                        "userId", playerId,
                        "teamId", teamId,
                        "teamName", teamName,
                        "action", "ASSIGN_PLAYER"
                ),
                authentication.getName(),
                System.currentTimeMillis()
        );

        messagingTemplate.convertAndSend("/topic/map/" + mapId, teamUpdateMessage);

        // Retourner le joueur connecté avec toutes les informations à jour
        return ResponseEntity.ok(updatedPlayer);
    }

}
