package com.airsoft.gamemapmaster.scenario.treasurehunt;

import com.airsoft.gamemapmaster.model.Scenario;
import com.airsoft.gamemapmaster.scenario.ScenarioModule;
import com.airsoft.gamemapmaster.scenario.treasurehunt.model.TreasureHuntScenario;
import com.airsoft.gamemapmaster.scenario.treasurehunt.service.TreasureHuntService;
import com.airsoft.gamemapmaster.service.ScenarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class TreasureHuntModule implements ScenarioModule {

    @Autowired
    private TreasureHuntService treasureHuntService;
    
    @Autowired
    private ScenarioService scenarioService;
    
    private static final String TYPE = "treasure_hunt";

    @Override
    @Transactional
    public boolean initialize(Scenario scenario) {
        try {
            // Définir le type de scénario
            scenario.setType(TYPE);
            
            // Sauvegarder le scénario
            Scenario savedScenario = scenarioService.save(scenario);
            
            // Créer un scénario de chasse au trésor associé
            TreasureHuntScenario treasureHuntScenario = new TreasureHuntScenario();
            treasureHuntScenario.setScenario(savedScenario);
            treasureHuntScenario.setTotalTreasures(0);
            treasureHuntScenario.setRequiredTreasures(0);
            treasureHuntScenario.setTeamBased(false);
            
            // Sauvegarder le scénario de chasse au trésor
            treasureHuntService.saveTreasureHuntScenario(treasureHuntScenario);
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional
    public boolean start(Long scenarioId) {
        try {
            Optional<Scenario> scenarioOpt = scenarioService.findById(scenarioId);
            if (scenarioOpt.isPresent()) {
                Scenario scenario = scenarioOpt.get();
                
                // Vérifier que c'est bien un scénario de chasse au trésor
                if (!TYPE.equals(scenario.getType())) {
                    return false;
                }
                
                // Activer le scénario
                scenarioService.activateScenario(scenarioId);
                
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional
    public boolean stop(Long scenarioId) {
        try {
            Optional<Scenario> scenarioOpt = scenarioService.findById(scenarioId);
            if (scenarioOpt.isPresent()) {
                Scenario scenario = scenarioOpt.get();
                
                // Vérifier que c'est bien un scénario de chasse au trésor
                if (!TYPE.equals(scenario.getType())) {
                    return false;
                }
                
                // Désactiver le scénario
                scenarioService.deactivateScenario(scenarioId);
                
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isCompleted(Long scenarioId) {
        try {
            Optional<Scenario> scenarioOpt = scenarioService.findById(scenarioId);
            if (scenarioOpt.isPresent()) {
                Scenario scenario = scenarioOpt.get();
                
                // Vérifier que c'est bien un scénario de chasse au trésor
                if (!TYPE.equals(scenario.getType())) {
                    return false;
                }
                
                // Récupérer le scénario de chasse au trésor associé
                Optional<TreasureHuntScenario> treasureHuntScenarioOpt = treasureHuntService.findByScenarioId(scenarioId);
                if (treasureHuntScenarioOpt.isPresent()) {
                    TreasureHuntScenario treasureHuntScenario = treasureHuntScenarioOpt.get();
                    
                    // Vérifier si tous les trésors requis ont été trouvés
                    // Cette logique peut être adaptée selon les besoins spécifiques
                    return false; // Pour l'instant, retourne toujours false car la logique complète n'est pas implémentée
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
