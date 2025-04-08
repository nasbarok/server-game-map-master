package com.airsoft.gamemapmaster.service;

import com.airsoft.gamemapmaster.model.ConnectedPlayer;
import com.airsoft.gamemapmaster.model.GameMap;
import com.airsoft.gamemapmaster.model.Team;
import com.airsoft.gamemapmaster.model.User;

import java.util.List;
import java.util.Optional;

public interface ConnectedPlayerService {
    
    /**
     * Connecte un joueur à une carte de jeu
     * @param gameMapId ID de la carte
     * @param userId ID de l'utilisateur
     * @param teamId ID de l'équipe (optionnel)
     * @return Le joueur connecté
     */
    ConnectedPlayer connectPlayerToMap(Long gameMapId, Long userId, Long teamId);
    
    /**
     * Déconnecte un joueur d'une carte de jeu
     * @param gameMapId ID de la carte
     * @param userId ID de l'utilisateur
     * @return true si le joueur a été déconnecté, false sinon
     */
    boolean disconnectPlayerFromMap(Long gameMapId, Long userId);
    
    /**
     * Récupère tous les joueurs connectés à une carte
     * @param gameMapId ID de la carte
     * @return Liste des joueurs connectés
     */
    List<ConnectedPlayer> getConnectedPlayersByMapId(Long gameMapId);
    
    /**
     * Vérifie si un joueur est déjà connecté à une carte
     * @param gameMapId ID de la carte
     * @param userId ID de l'utilisateur
     * @return true si le joueur est connecté, false sinon
     */
    boolean isPlayerConnectedToMap(Long gameMapId, Long userId);
    
    /**
     * Récupère un joueur connecté par son ID utilisateur et l'ID de la carte
     * @param gameMapId ID de la carte
     * @param userId ID de l'utilisateur
     * @return Le joueur connecté s'il existe
     */
    Optional<ConnectedPlayer> getConnectedPlayer(Long gameMapId, Long userId);
    
    /**
     * Déconnecte tous les joueurs d'une carte
     * @param gameMapId ID de la carte
     * @return Nombre de joueurs déconnectés
     */
    int disconnectAllPlayersFromMap(Long gameMapId);
    
    /**
     * Assigne un joueur à une équipe
     * @param gameMapId ID de la carte
     * @param userId ID de l'utilisateur
     * @param teamId ID de l'équipe
     * @return Le joueur connecté mis à jour
     */
    Optional<ConnectedPlayer> assignPlayerToTeam(Long gameMapId, Long userId, Long teamId);

    Team findTeamByUserAndMap(Long fromUserId, Long mapId);

    Optional<ConnectedPlayer> getConnectedPlayerByUserAndMap(Long userId, Long mapId);

    Optional<ConnectedPlayer> save(ConnectedPlayer player);

    List<ConnectedPlayer> findActiveConnectionsByUserId(Long id);

    boolean isPlayerConnectedToField(Long fieldId, Long fromUserId);

    public ConnectedPlayer connectPlayerToField(Long fieldId, Long fromUserId, Long teamId);

    List<ConnectedPlayer> getConnectedPlayersByFieldId(Long fieldId);

    boolean disconnectPlayerFromField(Long fieldId, Long id);

    int disconnectAllPlayersFromField(Long fieldId);

}
