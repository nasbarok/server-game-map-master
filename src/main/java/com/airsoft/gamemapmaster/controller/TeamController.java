package com.airsoft.gamemapmaster.controller;

import com.airsoft.gamemapmaster.model.Team;
import com.airsoft.gamemapmaster.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    @Autowired
    private TeamService teamService;

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
}
