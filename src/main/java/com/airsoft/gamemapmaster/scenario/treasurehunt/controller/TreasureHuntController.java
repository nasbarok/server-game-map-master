package com.airsoft.gamemapmaster.scenario.treasurehunt.controller;

import com.airsoft.gamemapmaster.model.Scenario;
import com.airsoft.gamemapmaster.scenario.treasurehunt.model.Treasure;
import com.airsoft.gamemapmaster.scenario.treasurehunt.model.TreasureFound;
import com.airsoft.gamemapmaster.scenario.treasurehunt.model.TreasureHuntScenario;
import com.airsoft.gamemapmaster.scenario.treasurehunt.service.TreasureHuntService;
import com.airsoft.gamemapmaster.service.ScenarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/scenarios/treasure-hunt")
public class TreasureHuntController {

    @Autowired
    private TreasureHuntService treasureHuntService;
    
    @Autowired
    private ScenarioService scenarioService;

    @PostMapping
    public ResponseEntity<TreasureHuntScenario> createTreasureHuntScenario(@RequestBody TreasureHuntScenario treasureHuntScenario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(treasureHuntService.saveTreasureHuntScenario(treasureHuntScenario));
    }

    @GetMapping("/{scenarioId}")
    public ResponseEntity<TreasureHuntScenario> getTreasureHuntScenario(@PathVariable Long scenarioId) {
        return treasureHuntService.findByScenarioId(scenarioId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TreasureHuntScenario> updateTreasureHuntScenario(@PathVariable Long id, @RequestBody TreasureHuntScenario treasureHuntScenario) {
        return treasureHuntService.findById(id)
                .map(existingScenario -> {
                    treasureHuntScenario.setId(id);
                    return ResponseEntity.ok(treasureHuntService.saveTreasureHuntScenario(treasureHuntScenario));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{treasureHuntId}/treasures")
    public ResponseEntity<Treasure> addTreasure(@PathVariable Long treasureHuntId, @RequestBody Treasure treasure) {
        return treasureHuntService.findById(treasureHuntId)
                .map(treasureHuntScenario -> {
                    treasure.setTreasureHuntScenario(treasureHuntScenario);
                    return ResponseEntity.status(HttpStatus.CREATED).body(treasureHuntService.saveTreasure(treasure));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{treasureHuntId}/treasures")
    public ResponseEntity<List<Treasure>> getTreasures(@PathVariable Long treasureHuntId) {
        return ResponseEntity.ok(treasureHuntService.findTreasuresByTreasureHuntId(treasureHuntId));
    }

    @GetMapping("/treasures/{treasureId}")
    public ResponseEntity<Treasure> getTreasureById(@PathVariable Long treasureId) {
        return treasureHuntService.findTreasureById(treasureId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/treasures/found")
    public ResponseEntity<TreasureFound> recordTreasureFound(@RequestBody TreasureFound treasureFound) {
        return ResponseEntity.status(HttpStatus.CREATED).body(treasureHuntService.saveTreasureFound(treasureFound));
    }

    @GetMapping("/treasures/found/user/{userId}")
    public ResponseEntity<List<TreasureFound>> getTreasuresFoundByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(treasureHuntService.findTreasuresFoundByUserId(userId));
    }

    @GetMapping("/treasures/found/team/{teamId}")
    public ResponseEntity<List<TreasureFound>> getTreasuresFoundByTeam(@PathVariable Long teamId) {
        return ResponseEntity.ok(treasureHuntService.findTreasuresFoundByTeamId(teamId));
    }

    @GetMapping("/treasures/qrcode/{qrCode}")
    public ResponseEntity<Treasure> getTreasureByQrCode(@PathVariable String qrCode) {
        return treasureHuntService.findTreasureByQrCode(qrCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
