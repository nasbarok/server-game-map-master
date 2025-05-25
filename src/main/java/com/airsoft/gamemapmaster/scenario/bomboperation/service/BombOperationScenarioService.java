package com.airsoft.gamemapmaster.scenario.bomboperation.service;

import com.airsoft.gamemapmaster.model.Scenario;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombOperationScenario;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombSite;

import java.util.List;
import java.util.Optional;

public interface BombOperationScenarioService {

    /**
     * Crée un nouveau scénario d'Opération Bombe
     * @param scenario Le scénario de base
     * @param bombTimer Temps avant explosion de la bombe en secondes
     * @param defuseTime Temps pour désamorcer en secondes
     * @param activeSites Nombre de sites actifs par round
     * @param attackTeamName Nom de l'équipe d'attaque
     * @param defenseTeamName Nom de l'équipe de défense
     * @param showZones Indique si les zones doivent être affichées sur la carte
     * @param showPointsOfInterest Indique si les points d'intérêt doivent être affichés sur la carte
     * @return Le scénario créé
     */
    BombOperationScenario createBombOperationScenario(
            Scenario scenario,
            Integer bombTimer,
            Integer defuseTime,
            Integer activeSites,
            String attackTeamName,
            String defenseTeamName,
            Boolean showZones,
            Boolean showPointsOfInterest
    );

    /**
     * Met à jour un scénario d'Opération Bombe existant
     * @param id ID du scénario à mettre à jour
     * @param bombTimer Temps avant explosion de la bombe en secondes
     * @param defuseTime Temps pour désamorcer en secondes
     * @param activeSites Nombre de sites actifs par round
     * @param attackTeamName Nom de l'équipe d'attaque
     * @param defenseTeamName Nom de l'équipe de défense
     * @param showZones Indique si les zones doivent être affichées sur la carte
     * @param showPointsOfInterest Indique si les points d'intérêt doivent être affichés sur la carte
     * @return Le scénario mis à jour
     */
    BombOperationScenario updateBombOperationScenario(
            Long id,
            Integer bombTimer,
            Integer defuseTime,
            Integer activeSites,
            String attackTeamName,
            String defenseTeamName,
            Boolean showZones,
            Boolean showPointsOfInterest
    );

    /**
     * Récupère un scénario d'Opération Bombe par son ID
     * @param id ID du scénario
     * @return Le scénario trouvé
     */
    BombOperationScenario getBombOperationScenarioById(Long id);

    /**
     * Récupère un scénario d'Opération Bombe par l'ID du scénario de base
     * @param scenarioId ID du scénario de base
     * @return Le scénario trouvé
     */
    BombOperationScenario getBombOperationScenarioByScenarioId(Long scenarioId);

    /**
     * Récupère le scénario d'Opération Bombe actif pour un scénario de base
     * @param scenarioId ID du scénario de base
     * @return Le scénario actif ou null si aucun n'est actif
     */
    BombOperationScenario getActiveBombOperationScenario(Long scenarioId);


    /**
     * Supprime un scénario d'Opération Bombe
     * @param id ID du scénario à supprimer
     */
    void deleteBombOperationScenario(Long id);

    /**
     * Ajoute un site de bombe à un scénario
     * @param scenarioId ID du scénario
     * @param name Nom du site (A, B, C, etc.)
     * @param latitude Latitude du site
     * @param longitude Longitude du site
     * @param radius Rayon du site en mètres
     * @return Le site créé
     */
    BombSite addBombSite(Long scenarioId,Long bombOperationScenarioId, String name, Double latitude, Double longitude, Double radius);

    /**
     * Met à jour un site de bombe
     * @param siteId ID du site
     * @param name Nom du site
     * @param latitude Latitude du site
     * @param longitude Longitude du site
     * @param radius Rayon du site en mètres
     * @return Le site mis à jour
     */
    BombSite updateBombSite(Long siteId, String name, Double latitude, Double longitude, Double radius);

    /**
     * Récupère un site de bombe par son ID
     * @param siteId ID du site
     * @return Le site trouvé
     */
    BombSite getBombSiteById(Long siteId);

    /**
     * Récupère tous les sites de bombe d'un scénario
     * @param scenarioId ID du scénario
     * @return Liste des sites
     */
    List<BombSite> getBombSitesByScenarioId(Long scenarioId);

    /**
     * Supprime un site de bombe
     * @param siteId ID du site à supprimer
     */
    void deleteBombSite(Long siteId);

    Optional<BombOperationScenario> findByScenarioId(Long scenarioId);

    BombOperationScenario saveBombOperationScenario(BombOperationScenario newScenario);
}
