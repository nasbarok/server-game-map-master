package com.airsoft.gamemapmaster.controller;

import com.airsoft.gamemapmaster.model.Scenario;
import com.airsoft.gamemapmaster.scenario.ScenarioModule;
import com.airsoft.gamemapmaster.scenario.ScenarioModuleRegistry;
import com.airsoft.gamemapmaster.service.ScenarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/scenario-modules")
public class ScenarioModuleController {

    @Autowired
    private ScenarioModuleRegistry moduleRegistry;
    
    @Autowired
    private ScenarioService scenarioService;

    @GetMapping("/types")
    public ResponseEntity<Set<String>> getSupportedScenarioTypes() {
        return ResponseEntity.ok(moduleRegistry.getSupportedTypes());
    }
    
    @PostMapping("/initialize/{type}")
    public ResponseEntity<?> initializeScenario(@PathVariable String type, @RequestBody Scenario scenario) {
        if (!moduleRegistry.isSupported(type)) {
            return ResponseEntity.badRequest().body("Type de scénario non supporté: " + type);
        }
        
        ScenarioModule module = moduleRegistry.getModule(type);
        boolean success = module.initialize(scenario);
        
        if (success) {
            return ResponseEntity.ok().body("Scénario initialisé avec succès");
        } else {
            return ResponseEntity.badRequest().body("Échec de l'initialisation du scénario");
        }
    }
    
    @PostMapping("/{scenarioId}/start")
    public ResponseEntity<?> startScenario(@PathVariable Long scenarioId) {
        return scenarioService.findById(scenarioId)
                .map(scenario -> {
                    String type = scenario.getType();
                    if (!moduleRegistry.isSupported(type)) {
                        return ResponseEntity.badRequest().body("Type de scénario non supporté: " + type);
                    }
                    
                    ScenarioModule module = moduleRegistry.getModule(type);
                    boolean success = module.start(scenarioId);
                    
                    if (success) {
                        return ResponseEntity.ok().body("Scénario démarré avec succès");
                    } else {
                        return ResponseEntity.badRequest().body("Échec du démarrage du scénario");
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/{scenarioId}/stop")
    public ResponseEntity<?> stopScenario(@PathVariable Long scenarioId) {
        return scenarioService.findById(scenarioId)
                .map(scenario -> {
                    String type = scenario.getType();
                    if (!moduleRegistry.isSupported(type)) {
                        return ResponseEntity.badRequest().body("Type de scénario non supporté: " + type);
                    }
                    
                    ScenarioModule module = moduleRegistry.getModule(type);
                    boolean success = module.stop(scenarioId);
                    
                    if (success) {
                        return ResponseEntity.ok().body("Scénario arrêté avec succès");
                    } else {
                        return ResponseEntity.badRequest().body("Échec de l'arrêt du scénario");
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{scenarioId}/completed")
    public ResponseEntity<?> isScenarioCompleted(@PathVariable Long scenarioId) {
        return scenarioService.findById(scenarioId)
                .map(scenario -> {
                    String type = scenario.getType();
                    if (!moduleRegistry.isSupported(type)) {
                        return ResponseEntity.badRequest().body("Type de scénario non supporté: " + type);
                    }
                    
                    ScenarioModule module = moduleRegistry.getModule(type);
                    boolean completed = module.isCompleted(scenarioId);
                    
                    return ResponseEntity.ok().body(completed);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
