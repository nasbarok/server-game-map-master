package com.airsoft.gamemapmaster.controller;

import com.airsoft.gamemapmaster.model.ConnectedPlayer;
import com.airsoft.gamemapmaster.model.DTO.TeamDTO;
import com.airsoft.gamemapmaster.model.GameMap;
import com.airsoft.gamemapmaster.model.Team;
import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.service.ConnectedPlayerService;
import com.airsoft.gamemapmaster.service.GameMapService;
import com.airsoft.gamemapmaster.service.TeamService;
import com.airsoft.gamemapmaster.service.UserService;
import com.airsoft.gamemapmaster.websocket.WebSocketMessage;
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
@RequestMapping("/api/teams")
public class TeamController {

    @Autowired
    private TeamService teamService;
    @Autowired
    private GameMapService gameMapService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private ConnectedPlayerService connectedPlayerService;

    @GetMapping
    public ResponseEntity<List<Team>> getAllTeams() {
        return ResponseEntity.ok(teamService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Team> getTeamById(@PathVariable Long id) {
        return teamService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Team> createTeam(@RequestBody Team team) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.save(team));
    }

    @PostMapping("/map/{mapId}/create")
    public ResponseEntity<TeamDTO> createTeamWithMap(@PathVariable Long mapId, @RequestBody Map<String, String> body) {
        String name = body.get("name");

        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        Optional<GameMap> optionalMap = gameMapService.findById(mapId);
        if (optionalMap.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Team team = new Team();
        team.setName(name);
        team.setGameMap(optionalMap.get());

        Team saved = teamService.save(team);
        return ResponseEntity.status(HttpStatus.CREATED).body(TeamDTO.fromEntity(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Team> updateTeam(@PathVariable Long id, @RequestBody Team team) {
        return teamService.findById(id)
                .map(existingTeam -> {
                    team.setId(id);
                    return ResponseEntity.ok(teamService.save(team));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
        return teamService.findById(id)
                .map(team -> {
                    teamService.deleteById(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/leader/{leaderId}")
    public ResponseEntity<List<Team>> getTeamsByLeaderId(@PathVariable Long leaderId) {
        return ResponseEntity.ok(teamService.findByLeaderId(leaderId));
    }
    
    @PostMapping("/{teamId}/members/{userId}")
    public ResponseEntity<Team> addMemberToTeam(@PathVariable Long teamId, @PathVariable Long userId) {
        return teamService.addMember(teamId, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{teamId}/members/{userId}")
    public ResponseEntity<Team> removeMemberFromTeam(@PathVariable Long teamId, @PathVariable Long userId) {
        return teamService.removeMember(teamId, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/map/{mapId}")
    public ResponseEntity<List<TeamDTO>> getTeamsByMap(@PathVariable Long mapId) {
        List<Team> teams = teamService.findTeamsByMap(mapId);
        List<TeamDTO> teamDtos = teams.stream()
                .map(TeamDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(teamDtos);
    }

    @PostMapping("/{mapId}/players/{userId}/remove-from-team")
    public ResponseEntity<?> removePlayerFromTeam(@PathVariable("mapId") Long mapId,
                                                  @PathVariable("userId") Long userId,
                                                  Authentication authentication) {
        String username = authentication.getName();
        Optional<User> currentUser = userService.findByUsername(username);

        // Vérifications d'autorisation...
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }

        // Vérifier si la carte existe
        Optional<GameMap> optionalMap = gameMapService.findById(mapId);
        if (optionalMap.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Carte non trouvée");
        }

        // Vérifier si l'utilisateur est le propriétaire
        if (!optionalMap.get().getOwner().getId().equals(currentUser.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Vous n'êtes pas autorisé à gérer cette carte");
        }

        // Récupérer le joueur connecté
        Optional<ConnectedPlayer> connectedPlayerOpt =
                connectedPlayerService.getConnectedPlayerByUserAndMap(userId, mapId);

        if (connectedPlayerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Joueur non connecté à cette carte");
        }

        // Retirer le joueur de l'équipe
        ConnectedPlayer player = connectedPlayerOpt.get();
        player.setTeam(null);
        connectedPlayerService.save(player);

        // Notification WebSocket
        WebSocketMessage teamUpdateMessage = new WebSocketMessage(
                "TEAM_UPDATE",
                Map.of(
                        "mapId", mapId,
                        "userId", userId,
                        "teamId", null,
                        "action", "REMOVE_FROM_TEAM"
                ),
                username,
                System.currentTimeMillis()
        );

        messagingTemplate.convertAndSend("/topic/map/" + mapId, teamUpdateMessage);

        return ResponseEntity.ok(player);
    }

}
