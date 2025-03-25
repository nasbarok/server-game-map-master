package com.airsoft.gamemapmaster.controller;

import com.airsoft.gamemapmaster.model.Scenario;
import com.airsoft.gamemapmaster.service.ScenarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scenarios")
public class ScenarioController {

    @Autowired
    private ScenarioService scenarioService;

    @GetMapping
    public ResponseEntity<List<Scenario>> getAllScenarios() {
        return ResponseEntity.ok(scenarioService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Scenario> getScenarioById(@PathVariable Long id) {
        return scenarioService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Scenario> createScenario(@RequestBody Scenario scenario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scenarioService.save(scenario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Scenario> updateScenario(@PathVariable Long id, @RequestBody Scenario scenario) {
        return scenarioService.findById(id)
                .map(existingScenario -> {
                    scenario.setId(id);
                    return ResponseEntity.ok(scenarioService.save(scenario));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScenario(@PathVariable Long id) {
        return scenarioService.findById(id)
                .map(scenario -> {
                    scenarioService.deleteById(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/map/{gameMapId}")
    public ResponseEntity<List<Scenario>> getScenariosByGameMapId(@PathVariable Long gameMapId) {
        return ResponseEntity.ok(scenarioService.findByGameMapId(gameMapId));
    }
    
    @GetMapping("/creator/{creatorId}")
    public ResponseEntity<List<Scenario>> getScenariosByCreatorId(@PathVariable Long creatorId) {
        return ResponseEntity.ok(scenarioService.findByCreatorId(creatorId));
    }
    
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Scenario>> getScenariosByType(@PathVariable String type) {
        return ResponseEntity.ok(scenarioService.findByType(type));
    }
    
    @PutMapping("/{id}/activate")
    public ResponseEntity<Scenario> activateScenario(@PathVariable Long id) {
        return scenarioService.activateScenario(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Scenario> deactivateScenario(@PathVariable Long id) {
        return scenarioService.deactivateScenario(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
