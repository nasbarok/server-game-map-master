package com.airsoft.gamemapmaster.service.impl;

import com.airsoft.gamemapmaster.model.GameMap;
import com.airsoft.gamemapmaster.model.Team;
import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.repository.GameMapRepository;
import com.airsoft.gamemapmaster.repository.TeamRepository;
import com.airsoft.gamemapmaster.repository.UserRepository;
import com.airsoft.gamemapmaster.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TeamServiceImpl implements TeamService {

    @Autowired
    private TeamRepository teamRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameMapRepository gameMapRepository;

    @Override
    public List<Team> findAll() {
        return teamRepository.findAll();
    }

    @Override
    public Optional<Team> findById(Long id) {
        return teamRepository.findById(id);
    }

    @Override
    public List<Team> findByLeaderId(Long leaderId) {
        return teamRepository.findByLeaderId(leaderId);
    }

    @Override
    @Transactional
    public Team save(Team team) {
        // Vérifier si la carte existe
        if (team.getGameMap() != null && team.getGameMap().getId() != null) {
            Optional<GameMap> gameMapOpt = gameMapRepository.findById(team.getGameMap().getId());
            if (gameMapOpt.isPresent()) {
                // Assurer que la référence à la carte est correcte
                team.setGameMap(gameMapOpt.get());
            }
        }
        return teamRepository.save(team);
    }

    @Override
    public void deleteById(Long id) {
        teamRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Optional<Team> addMember(Long teamId, Long userId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (teamOpt.isPresent() && userOpt.isPresent()) {
            Team team = teamOpt.get();
            User user = userOpt.get();
            team.getMembers().add(user);
            return Optional.of(teamRepository.save(team));
        }
        
        return Optional.empty();
    }

    @Override
    @Transactional
    public Optional<Team> removeMember(Long teamId, Long userId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (teamOpt.isPresent() && userOpt.isPresent()) {
            Team team = teamOpt.get();
            User user = userOpt.get();
            team.getMembers().remove(user);
            return Optional.of(teamRepository.save(team));
        }
        
        return Optional.empty();
    }

    @Override
    public List<Team> findTeamsByMap(Long mapId) {
        return teamRepository.findByGameMapId(mapId);
    }

    @Override
    public Optional<Team> findTeamByUserId(Long userId) {
        return teamRepository.findByMembersId(userId);
    }
}
