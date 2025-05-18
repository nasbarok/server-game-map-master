package com.airsoft.gamemapmaster.scenario.bomboperation.service;

import com.airsoft.gamemapmaster.model.Scenario;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombOperationScenario;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombSite;

import java.util.List;

public interface BombOperationScenarioService {

    /**
     * Crée un nouveau scénario d'Opération Bombe
     * @param scenario Le scénario de base
     * @param roundDuration Durée d'un round en secondes
     * @param bombTimer Temps avant explosion de la bombe en secondes
     * @param defuseTime Temps pour désamorcer en secondes
     * @param maxRounds Nombre maximum de rounds
     * @param activeSites Nombre de sites actifs par round
     * @param attackTeamName Nom de l'équipe d'attaque
     * @param defenseTeamName Nom de l'équipe de défense
     * @return Le scénario créé
     */
    BombOperationScenario createBombOperationScenario(
            Scenario scenario,
            Integer roundDuration,
            Integer bombTimer,
            Integer defuseTime,
            Integer maxRounds,
            Integer activeSites,
            String attackTeamName,
            String defenseTeamName
    );

    /**
     * Met à jour un scénario d'Opération Bombe existant
     * @param id ID du scénario à mettre à jour
     * @param roundDuration Durée d'un round en secondes
     * @param bombTimer Temps avant explosion de la bombe en secondes
     * @param defuseTime Temps pour désamorcer en secondes
     * @param maxRounds Nombre maximum de rounds
     * @param activeSites Nombre de sites actifs par round
     * @param attackTeamName Nom de l'équipe d'attaque
     * @param defenseTeamName Nom de l'équipe de défense
     * @return Le scénario mis à jour
     */
    BombOperationScenario updateBombOperationScenario(
            Long id,
            Integer roundDuration,
            Integer bombTimer,
            Integer defuseTime,
            Integer maxRounds,
            Integer activeSites,
            String attackTeamName,
            String defenseTeamName
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
     * Active un scénario d'Opération Bombe (et désactive les autres)
     * @param id ID du scénario à activer
     * @return Le scénario activé
     */
    BombOperationScenario activateBombOperationScenario(Long id);

    /**
     * Désactive un scénario d'Opération Bombe
     * @param id ID du scénario à désactiver
     * @return Le scénario désactivé
     */
    BombOperationScenario deactivateBombOperationScenario(Long id);

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
    BombSite addBombSite(Long scenarioId, String name, Double latitude, Double longitude, Double radius);

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
}
