package com.airsoft.gamemapmaster.service;

import com.airsoft.gamemapmaster.model.Team;

import java.util.List;
import java.util.Optional;

public interface TeamService {
    List<Team> findAll();
    Optional<Team> findById(Long id);
    List<Team> findByLeaderId(Long leaderId);
    Team save(Team team);
    void deleteById(Long id);
    Optional<Team> addMember(Long teamId, Long userId);
    Optional<Team> removeMember(Long teamId, Long userId);

    List<Team> findTeamsByMap(Long mapId);

    Optional<Team> findTeamByUserId(Long userId);
}
