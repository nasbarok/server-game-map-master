package com.airsoft.gamemapmaster.service.impl;

import com.airsoft.gamemapmaster.model.*;
import com.airsoft.gamemapmaster.repository.ConnectedPlayerRepository;
import com.airsoft.gamemapmaster.repository.GameMapRepository;
import com.airsoft.gamemapmaster.repository.TeamRepository;
import com.airsoft.gamemapmaster.repository.UserRepository;
import com.airsoft.gamemapmaster.repository.FieldRepository;
import com.airsoft.gamemapmaster.service.ConnectedPlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ConnectedPlayerServiceImpl implements ConnectedPlayerService {

    @Autowired
    private ConnectedPlayerRepository connectedPlayerRepository;
    
    @Autowired
    private GameMapRepository gameMapRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private FieldRepository fieldRepository;

    @Override
    @Transactional
    public ConnectedPlayer connectPlayerToMap(Long gameMapId, Long userId, Long teamId) {
        // Vérifier si le joueur est déjà connecté à cette carte
        if (connectedPlayerRepository.existsByUserIdAndGameMapIdAndActiveTrue(userId, gameMapId)) {
            Optional<ConnectedPlayer> existingPlayer = connectedPlayerRepository.findByUserIdAndGameMapIdAndActiveTrue(userId, gameMapId);
            return existingPlayer.orElse(null);
        }
        
        // Récupérer les entités nécessaires
        Optional<GameMap> gameMap = gameMapRepository.findById(gameMapId);
        Optional<User> user = userRepository.findById(userId);
        
        if (gameMap.isEmpty() || user.isEmpty()) {
            return null;
        }
        
        // Créer un nouveau joueur connecté
        ConnectedPlayer connectedPlayer = new ConnectedPlayer();
        connectedPlayer.setGameMap(gameMap.get());
        connectedPlayer.setUser(user.get());
        
        // Assigner à une équipe si spécifiée
        if (teamId != null) {
            Optional<Team> team = teamRepository.findById(teamId);
            team.ifPresent(connectedPlayer::setTeam);
        }
        
        return connectedPlayerRepository.save(connectedPlayer);
    }

    @Override
    @Transactional
    public boolean disconnectPlayerFromMap(Long gameMapId, Long userId) {
        Optional<ConnectedPlayer> connectedPlayer = connectedPlayerRepository.findByUserIdAndGameMapIdAndActiveTrue(userId, gameMapId);
        
        if (connectedPlayer.isPresent()) {
            ConnectedPlayer player = connectedPlayer.get();
            player.setActive(false);
            connectedPlayerRepository.save(player);
            return true;
        }
        
        return false;
    }

    @Override
    public List<ConnectedPlayer> getConnectedPlayersByMapId(Long gameMapId) {
        return connectedPlayerRepository.findByGameMapIdAndActiveTrue(gameMapId);
    }

    @Override
    public boolean isPlayerConnectedToMap(Long gameMapId, Long userId) {
        return connectedPlayerRepository.existsByUserIdAndGameMapIdAndActiveTrue(userId, gameMapId);
    }

    @Override
    public Optional<ConnectedPlayer> getConnectedPlayer(Long gameMapId, Long userId) {
        return connectedPlayerRepository.findByUserIdAndGameMapIdAndActiveTrue(userId, gameMapId);
    }

    @Override
    @Transactional
    public int disconnectAllPlayersFromMap(Long gameMapId) {
        List<ConnectedPlayer> connectedPlayers = connectedPlayerRepository.findByGameMapIdAndActiveTrue(gameMapId);
        
        for (ConnectedPlayer player : connectedPlayers) {
            player.setActive(false);
            connectedPlayerRepository.save(player);
        }
        
        return connectedPlayers.size();
    }

    @Override
    @Transactional
    public Optional<ConnectedPlayer> assignPlayerToTeam(Long gameMapId, Long userId, Long teamId) {
        Optional<ConnectedPlayer> connectedPlayer = connectedPlayerRepository.findByUserIdAndGameMapIdAndActiveTrue(userId, gameMapId);
        Optional<Team> team = teamRepository.findById(teamId);
        
        if (connectedPlayer.isPresent() && team.isPresent()) {
            ConnectedPlayer player = connectedPlayer.get();
            player.setTeam(team.get());
            return Optional.of(connectedPlayerRepository.save(player));
        }
        
        return Optional.empty();
    }

    @Override
    public Team findTeamByUserAndMap(Long fromUserId, Long mapId) {
        return connectedPlayerRepository.findTeamByUserAndMap(fromUserId, mapId);
    }

    @Override
    public Optional<ConnectedPlayer> getConnectedPlayerByUserAndMap(Long userId, Long mapId) {
        return connectedPlayerRepository.findByUserIdAndGameMapIdAndActiveTrue(userId, mapId);
    }

    @Override
    public Optional<ConnectedPlayer> save(ConnectedPlayer player) {
        return Optional.of(connectedPlayerRepository.save(player));
    }

    @Override
    public List<ConnectedPlayer> findActiveConnectionsByUserId(Long id) {
        return connectedPlayerRepository.findByUserIdAndActiveTrue(id);
    }

    @Override
    public boolean isPlayerConnectedToField(Long fieldId, Long fromUserId) {
        return connectedPlayerRepository.existsByUserIdAndFieldIdAndActiveTrue(fromUserId, fieldId);
    }

    @Override
    public ConnectedPlayer connectPlayerToField(Long fieldId, Long fromUserId, Long teamId) {
        // Vérifier si le joueur est déjà connecté à cette carte
        if (connectedPlayerRepository.existsByUserIdAndFieldIdAndActiveTrue(fromUserId, fieldId)) {
            Optional<ConnectedPlayer> existingPlayer = connectedPlayerRepository.findByUserIdAndFieldIdAndActiveTrue(fromUserId, fieldId);
            return existingPlayer.orElse(null);
        }

        // Récupérer les entités nécessaires
        Optional<Field> field = fieldRepository.findById(fieldId);
        Optional<User> user = userRepository.findById(fromUserId);
        Optional<GameMap> gameMap = gameMapRepository.findFirstByFieldId(fieldId);

        if (field.isEmpty() || user.isEmpty()) {
            return null;
        }

        // Créer un nouveau joueur connecté
        ConnectedPlayer connectedPlayer = new ConnectedPlayer();
        connectedPlayer.setField(field.get());
        connectedPlayer.setUser(user.get());
        connectedPlayer.setGameMap(gameMap.get());

        // Assigner à une équipe si spécifiée
        if (teamId != null) {
            Optional<Team> team = teamRepository.findById(teamId);
            team.ifPresent(connectedPlayer::setTeam);
        }

        return connectedPlayerRepository.save(connectedPlayer);
    }

}
