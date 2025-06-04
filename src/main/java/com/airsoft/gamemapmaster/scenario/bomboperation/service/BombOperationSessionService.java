package com.airsoft.gamemapmaster.scenario.bomboperation.service;

import com.airsoft.gamemapmaster.scenario.bomboperation.dto.BombOperationSessionDto;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombOperationSession;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombSite;

import java.util.List;
import java.util.Map;

public interface BombOperationSessionService {

    /**
     * Crée une nouvelle session pour un scénario d'Opération Bombe
     * @param scenarioId ID du scénario d'Opération Bombe
     * @param gameSessionId ID de la session de jeu
     * @return La session créée
     */
    BombOperationSessionDto createBombOperationSession(Long scenarioId, Long gameSessionId);

    /**
     * Récupère une session par son ID
     * @param sessionId ID de la session
     * @return La session trouvée
     */
    BombOperationSession getSessionById(Long sessionId);

    /**
     * Récupère une session par l'ID de la session de jeu
     * @param gameSessionId ID de la session de jeu
     * @return La session trouvée
     */
    BombOperationSessionDto getBombOperationSessionDtoByGameSessionId(Long gameSessionId);
    public BombOperationSession getBombOperationSessionByGameSessionId(Long gameSessionId);

    /**
     * Pose une bombe sur un site
     * @param sessionId ID de la session
     * @param userId ID de l'utilisateur qui pose la bombe
     * @param siteId ID du site de bombe
     * @param latitude Latitude actuelle du joueur
     * @param longitude Longitude actuelle du joueur
     * @return La session mise à jour
     */
    BombOperationSession plantBomb(Long sessionId, Long userId, Long siteId, Double latitude, Double longitude);

    /**
     * Commence le désamorçage d'une bombe
     * @param sessionId ID de la session
     * @param userId ID de l'utilisateur qui désamorce
     * @param latitude Latitude actuelle du joueur
     * @param longitude Longitude actuelle du joueur
     * @return La session mise à jour
     */
    BombOperationSession startDefusing(Long sessionId, Long userId, Double latitude, Double longitude);

    /**
     * Termine le désamorçage d'une bombe
     * @param sessionId ID de la session
     * @param userId ID de l'utilisateur qui a désamorcé
     * @return La session mise à jour
     */
    BombOperationSession finishDefusing(Long sessionId, Long userId);

    /**
     * Fait exploser la bombe (appelé automatiquement après le délai)
     * @param sessionId ID de la session
     * @return La session mise à jour
     */
    BombOperationSession explodeBomb(Long sessionId);


    /**
     * Termine la partie
     * @param sessionId ID de la session
     * @return La session mise à jour
     */
    BombOperationSession endGame(Long sessionId);

    /**
     * Vérifie si un joueur est dans un site de bombe actif
     * @param sessionId ID de la session
     * @param latitude Latitude du joueur
     * @param longitude Longitude du joueur
     * @return Le site de bombe si le joueur est dedans, null sinon
     */
    BombSite isPlayerInActiveBombSite(Long sessionId, Double latitude, Double longitude);

    /**
     * Récupère tous les sites de bombe actifs pour une session
     * @param sessionId ID de la session
     * @return Liste des sites actifs
     */
    List<BombSite> getActiveBombSites(Long sessionId);

    /**
     * Supprime une session
     * @param sessionId ID de la session à supprimer
     */
    void deleteSession(Long sessionId);

    Object getGameSessionState(Long gameSessionId);

    void saveTeamRoles(Long gameSessionId, Map<String, String> teamRoles);

    Map<String, String> getTeamRoles(Long gameSessionId);

    List<BombSite> selectAndActivateRandomSites(Long gameSessionId);
}
