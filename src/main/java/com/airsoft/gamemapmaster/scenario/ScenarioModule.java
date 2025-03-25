package com.airsoft.gamemapmaster.scenario;

import com.airsoft.gamemapmaster.model.Scenario;

/**
 * Interface de base pour tous les types de scénarios.
 * Chaque nouveau type de scénario doit implémenter cette interface.
 */
public interface ScenarioModule {
    
    /**
     * Initialise le scénario avec les paramètres par défaut
     * @param scenario Le scénario de base à initialiser
     * @return true si l'initialisation a réussi, false sinon
     */
    boolean initialize(Scenario scenario);
    
    /**
     * Démarre le scénario
     * @param scenarioId L'identifiant du scénario à démarrer
     * @return true si le démarrage a réussi, false sinon
     */
    boolean start(Long scenarioId);
    
    /**
     * Arrête le scénario
     * @param scenarioId L'identifiant du scénario à arrêter
     * @return true si l'arrêt a réussi, false sinon
     */
    boolean stop(Long scenarioId);
    
    /**
     * Vérifie si le scénario est terminé
     * @param scenarioId L'identifiant du scénario à vérifier
     * @return true si le scénario est terminé, false sinon
     */
    boolean isCompleted(Long scenarioId);
    
    /**
     * Retourne le type de scénario
     * @return Le type de scénario (ex: "treasure_hunt", "capture_flag", etc.)
     */
    String getType();
}
