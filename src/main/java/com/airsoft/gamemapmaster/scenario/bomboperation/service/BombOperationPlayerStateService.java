package com.airsoft.gamemapmaster.scenario.bomboperation.service;

import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombOperationPlayerState;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombOperationTeam;

import java.util.List;

public interface BombOperationPlayerStateService {

    /**
     * Crée ou met à jour l'état d'un joueur pour une session
     * @param sessionId ID de la session
     * @param userId ID de l'utilisateur
     * @param team Équipe du joueur
     * @param hasDefuseKit Si le joueur a un kit de désamorçage
     * @return L'état du joueur créé ou mis à jour
     */
    BombOperationPlayerState createOrUpdatePlayerState(Long sessionId, Long userId, BombOperationTeam team, Boolean hasDefuseKit);

    /**
     * Récupère l'état d'un joueur pour une session
     * @param sessionId ID de la session
     * @param userId ID de l'utilisateur
     * @return L'état du joueur
     */
    BombOperationPlayerState getPlayerState(Long sessionId, Long userId);

    /**
     * Récupère tous les états des joueurs pour une session
     * @param sessionId ID de la session
     * @return Liste des états des joueurs
     */
    List<BombOperationPlayerState> getAllPlayerStates(Long sessionId);

    /**
     * Récupère tous les états des joueurs d'une équipe pour une session
     * @param sessionId ID de la session
     * @param team Équipe des joueurs
     * @return Liste des états des joueurs de l'équipe
     */
    List<BombOperationPlayerState> getPlayerStatesByTeam(Long sessionId, BombOperationTeam team);

    /**
     * Marque un joueur comme mort
     * @param sessionId ID de la session
     * @param userId ID de l'utilisateur
     * @return L'état du joueur mis à jour
     */
    BombOperationPlayerState killPlayer(Long sessionId, Long userId);

    /**
     * Marque un joueur comme vivant
     * @param sessionId ID de la session
     * @param userId ID de l'utilisateur
     * @return L'état du joueur mis à jour
     */
    BombOperationPlayerState revivePlayer(Long sessionId, Long userId);

    /**
     * Donne un kit de désamorçage à un joueur
     * @param sessionId ID de la session
     * @param userId ID de l'utilisateur
     * @return L'état du joueur mis à jour
     */
    BombOperationPlayerState giveDefuseKit(Long sessionId, Long userId);

    /**
     * Retire un kit de désamorçage à un joueur
     * @param sessionId ID de la session
     * @param userId ID de l'utilisateur
     * @return L'état du joueur mis à jour
     */
    BombOperationPlayerState removeDefuseKit(Long sessionId, Long userId);

    /**
     * Incrémente le compteur de bombes posées pour un joueur
     * @param sessionId ID de la session
     * @param userId ID de l'utilisateur
     * @return L'état du joueur mis à jour
     */
    BombOperationPlayerState incrementBombsPlanted(Long sessionId, Long userId);

    /**
     * Incrémente le compteur de bombes désamorcées pour un joueur
     * @param sessionId ID de la session
     * @param userId ID de l'utilisateur
     * @return L'état du joueur mis à jour
     */
    BombOperationPlayerState incrementBombsDefused(Long sessionId, Long userId);

    /**
     * Réinitialise tous les états des joueurs pour une session (pour un nouveau round)
     * @param sessionId ID de la session
     * @return Liste des états des joueurs réinitialisés
     */
    List<BombOperationPlayerState> resetAllPlayerStates(Long sessionId);

    /**
     * Supprime l'état d'un joueur
     * @param sessionId ID de la session
     * @param userId ID de l'utilisateur
     */
    void deletePlayerState(Long sessionId, Long userId);

    /**
     * Supprime tous les états des joueurs pour une session
     * @param sessionId ID de la session
     */
    void deleteAllPlayerStates(Long sessionId);
}
