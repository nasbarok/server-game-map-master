package com.airsoft.gamemapmaster.controller;

import com.airsoft.gamemapmaster.model.DTO.ScenarioDTO;
import com.airsoft.gamemapmaster.model.GameMap;
import com.airsoft.gamemapmaster.model.Scenario;
import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.scenario.treasurehunt.model.TreasureHuntScenario;
import com.airsoft.gamemapmaster.scenario.treasurehunt.service.TreasureHuntService;
import com.airsoft.gamemapmaster.service.GameMapService;
import com.airsoft.gamemapmaster.service.ScenarioService;
import com.airsoft.gamemapmaster.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/scenarios")
public class ScenarioController {

    private static final Logger logger = LoggerFactory.getLogger(ScenarioController.class);

    @Autowired
    private ScenarioService scenarioService;
    @Autowired
    private UserService userService;
    @Autowired
    private TreasureHuntService treasureHuntService;

    @GetMapping
    public ResponseEntity<List<Scenario>> getAllScenarios() {
        return ResponseEntity.ok(scenarioService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScenarioDTO> getScenarioById(@PathVariable Long id) {
        Optional<Scenario> scenarioOpt = scenarioService.findById(id);
        if (scenarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Scenario scenario = scenarioOpt.get();
        Optional<TreasureHuntScenario> treasureOpt = treasureHuntService.findByScenarioId(scenario.getId());
        TreasureHuntScenario treasure = treasureOpt.orElse(null);

        ScenarioDTO dto = new ScenarioDTO(scenario, treasure);
        return ResponseEntity.ok(dto);
    }
    @PostMapping
    public ResponseEntity<Scenario> createScenario(@RequestBody Scenario scenario) {
        // Logs détaillés
        logger.info("📥 Création scénario reçu:");
        logger.info("➡️ Nom: {}", scenario.getName());
        logger.info("➡️ Description: {}", scenario.getDescription());
        logger.info("➡️ Type: {}", scenario.getType());
        logger.info("➡️ GameMap: {}", scenario.getGameMap() != null ? scenario.getGameMap().getId() : "❌ null");
        logger.info("➡️ Creator: {}", scenario.getCreator() != null ? scenario.getCreator().getId() : "❌ null");

        Scenario saved = scenarioService.save(scenario);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
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

    @GetMapping("/owner/self")
    public ResponseEntity<List<Scenario>> getMyScenarios(Principal principal) {
        String username = principal.getName();
        Optional<User> user = userService.findByUsername(username);

        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Scenario> scenarioList = scenarioService.findByOwnerId(user.get().getId());
        logger.info("Found " + scenarioList.size() + " scenarios for user " + user.get().getUsername());
        return ResponseEntity.ok(scenarioList);
    }

    @GetMapping("/owner/self/full")
    public ResponseEntity<List<ScenarioDTO>> getMyScenariosWithSpecificModules(Principal principal) {
        String username = principal.getName();
        Optional<User> userOpt = userService.findByUsername(username);

        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        List<Scenario> scenarioList = scenarioService.findByOwnerId(user.getId());

        List<ScenarioDTO> fullList = new ArrayList<>();

        for (Scenario scenario : scenarioList) {
            Optional<TreasureHuntScenario> treasureHuntScenarioOpt = treasureHuntService.findByScenarioId(scenario.getId());
            TreasureHuntScenario treasureHuntScenario = treasureHuntScenarioOpt.orElse(null);

            ScenarioDTO dto = new ScenarioDTO(scenario, treasureHuntScenario);
            fullList.add(dto);
        }

        return ResponseEntity.ok(fullList);
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
