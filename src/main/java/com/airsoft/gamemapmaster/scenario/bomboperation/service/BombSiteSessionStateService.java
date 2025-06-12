package com.airsoft.gamemapmaster.scenario.bomboperation.service;

import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombSite;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombSiteSessionState;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombSiteStatus;

import java.util.List;
import java.util.Optional;

/**
 * Interface du service pour la gestion des états des sites de bombe dans les sessions
 */
public interface BombSiteSessionStateService {
    
    /**
     * Crée les états de sites pour une session à partir d'une liste de BombSites
     * @param gameSessionId ID de la session de jeu
     * @param bombSites Liste des sites de bombe à cloner
     * @return Liste des états créés
     */
    List<BombSiteSessionState> createSessionStatesFromBombSites(Long gameSessionId, List<BombSite> bombSites);
    
    /**
     * Sélectionne et active aléatoirement un nombre donné de sites pour une session
     * @param gameSessionId ID de la session de jeu
     * @param numberOfSitesToActivate Nombre de sites à activer
     * @return Liste des sites activés
     */
    List<BombSiteSessionState> selectAndActivateRandomSites(Long gameSessionId, int numberOfSitesToActivate);
    
    /**
     * Obtient tous les états de sites pour une session
     * @param gameSessionId ID de la session de jeu
     * @return Liste de tous les états de sites
     */
    List<BombSiteSessionState> getAllSessionStates(Long gameSessionId);
    
    /**
     * Obtient tous les sites actifs pour une session
     * @param gameSessionId ID de la session de jeu
     * @return Liste des sites actifs
     */
    List<BombSiteSessionState> getActiveSites(Long gameSessionId);
    
    /**
     * Obtient tous les sites armés pour une session
     * @param gameSessionId ID de la session de jeu
     * @return Liste des sites armés
     */
    List<BombSiteSessionState> getArmedSites(Long gameSessionId);
    
    /**
     * Obtient tous les sites explosés pour une session
     * @param gameSessionId ID de la session de jeu
     * @return Liste des sites explosés
     */
    List<BombSiteSessionState> getExplodedSites(Long gameSessionId);
    
    /**
     * Obtient tous les sites désarmés pour une session
     * @param gameSessionId ID de la session de jeu
     * @return Liste des sites désarmés
     */
    List<BombSiteSessionState> getDisarmedSites(Long gameSessionId);
    
    /**
     * Trouve un état de site spécifique par session et ID de site original
     * @param gameSessionId ID de la session de jeu
     * @param originalBombSiteId ID du site de bombe original
     * @return État du site si trouvé
     */
    Optional<BombSiteSessionState> findByGameSessionAndOriginalSite(Long gameSessionId, Long originalBombSiteId);
    
    /**
     * Active un site spécifique
     * @param gameSessionId ID de la session de jeu
     * @param originalBombSiteId ID du site de bombe original
     * @return État du site activé
     */
    BombSiteSessionState activateSite(Long gameSessionId, Long originalBombSiteId);
    
    /**
     * Désactive un site spécifique
     * @param gameSessionId ID de la session de jeu
     * @param originalBombSiteId ID du site de bombe original
     * @return État du site désactivé
     */
    BombSiteSessionState deactivateSite(Long gameSessionId, Long originalBombSiteId);
    
    /**
     * Arme une bombe sur un site spécifique
     * @param gameSessionId ID de la session de jeu
     * @param originalBombSiteId ID du site de bombe original
     * @param userId ID du joueur qui arme la bombe
     * @param bombTimerSeconds Durée du timer en secondes
     * @return État du site armé
     */
    BombSiteSessionState armBomb(Long gameSessionId, Long originalBombSiteId, Long userId, Integer bombTimerSeconds);
    
    /**
     * Désarme une bombe sur un site spécifique
     * @param gameSessionId ID de la session de jeu
     * @param originalBombSiteId ID du site de bombe original
     * @param userId ID du joueur qui désarme la bombe
     * @return État du site désarmé
     */
    BombSiteSessionState disarmBomb(Long gameSessionId, Long originalBombSiteId, Long userId);
    
    /**
     * Fait exploser une bombe sur un site spécifique
     * @param gameSessionId ID de la session de jeu
     * @param originalBombSiteId ID du site de bombe original
     * @return État du site explosé
     */
    BombSiteSessionState explodeBomb(Long gameSessionId, Long originalBombSiteId);
    
    /**
     * Vérifie et fait exploser automatiquement les bombes dont le timer a expiré
     * @param gameSessionId ID de la session de jeu
     * @return Liste des sites qui ont explosé
     */
    List<BombSiteSessionState> checkAndExplodeExpiredBombs(Long gameSessionId);
    
    /**
     * Supprime tous les états de sites pour une session
     * @param gameSessionId ID de la session de jeu
     */
    void deleteAllSessionStates(Long gameSessionId);
    
    /**
     * Vérifie si une session a des sites actifs
     * @param gameSessionId ID de la session de jeu
     * @return true si la session a des sites actifs
     */
    boolean hasActiveSites(Long gameSessionId);
    
    /**
     * Vérifie si une session a des sites armés
     * @param gameSessionId ID de la session de jeu
     * @return true si la session a des sites armés
     */
    boolean hasArmedSites(Long gameSessionId);
    
    /**
     * Obtient les statistiques des sites par statut pour une session
     * @param gameSessionId ID de la session de jeu
     * @return Map avec le nombre de sites par statut
     */
    java.util.Map<BombSiteStatus, Long> getSiteStatistics(Long gameSessionId);

    BombSiteSessionState getBombSiteStatus(Long bombSiteStatusId);
}

