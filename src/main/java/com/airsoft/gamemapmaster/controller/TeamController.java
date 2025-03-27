package com.airsoft.gamemapmaster.controller;

import com.airsoft.gamemapmaster.model.DTO.TeamDTO;
import com.airsoft.gamemapmaster.model.GameMap;
import com.airsoft.gamemapmaster.model.Team;
import com.airsoft.gamemapmaster.service.GameMapService;
import com.airsoft.gamemapmaster.service.TeamService;
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
@RequestMapping("/api/teams")
public class TeamController {

    @Autowired
    private TeamService teamService;
    @Autowired
    private GameMapService gameMapService;
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
}
