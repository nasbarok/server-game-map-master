package com.airsoft.gamemapmaster.scenario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Service qui gère les différents modules de scénarios
 */
@Service
public class ScenarioModuleRegistry {

    private final Map<String, ScenarioModule> modules = new HashMap<>();
    
    @Autowired
    public ScenarioModuleRegistry(Set<ScenarioModule> moduleSet) {
        moduleSet.forEach(module -> modules.put(module.getType(), module));
    }
    
    /**
     * Récupère un module de scénario par son type
     * @param type Le type de scénario
     * @return Le module correspondant ou null si non trouvé
     */
    public ScenarioModule getModule(String type) {
        return modules.get(type);
    }
    
    /**
     * Vérifie si un type de scénario est supporté
     * @param type Le type de scénario
     * @return true si le type est supporté, false sinon
     */
    public boolean isSupported(String type) {
        return modules.containsKey(type);
    }
    
    /**
     * Retourne tous les types de scénarios supportés
     * @return L'ensemble des types de scénarios supportés
     */
    public Set<String> getSupportedTypes() {
        return modules.keySet();
    }
}
