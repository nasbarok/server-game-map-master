package com.airsoft.gamemapmaster.scenario.treasurehunt.controller;

import com.airsoft.gamemapmaster.model.Scenario;
import com.airsoft.gamemapmaster.model.Team;
import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.scenario.treasurehunt.model.Treasure;
import com.airsoft.gamemapmaster.scenario.treasurehunt.model.TreasureFound;
import com.airsoft.gamemapmaster.scenario.treasurehunt.model.TreasureHuntScenario;
import com.airsoft.gamemapmaster.scenario.treasurehunt.service.TreasureHuntService;
import com.airsoft.gamemapmaster.service.GameSessionService;
import com.airsoft.gamemapmaster.service.ScenarioService;
import com.airsoft.gamemapmaster.service.TeamService;
import com.airsoft.gamemapmaster.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/scenarios/treasure-hunt")
public class TreasureHuntController {
    private static final Logger logger = LoggerFactory.getLogger(TreasureHuntController.class);

    @Autowired
    private TreasureHuntService treasureHuntService;

    @Autowired
    private ScenarioService scenarioService;

    @Autowired
    private UserService userService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private GameSessionService gameSessionService;

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

    // Nouveaux endpoints pour la gestion des QR codes et des scores

    @PostMapping("/{treasureHuntId}/treasures/batch")
    public ResponseEntity<List<Treasure>> createTreasuresBatch(
            @PathVariable Long treasureHuntId,
            @RequestBody Map<String, Object> request) {

        int count = Integer.parseInt(request.getOrDefault("count", 10).toString());
        int defaultValue = Integer.parseInt(request.getOrDefault("defaultValue", 50).toString());
        String defaultSymbol = (String) request.getOrDefault("defaultSymbol", "💰");

        try {
            List<Treasure> treasures = treasureHuntService.createTreasuresBatch(treasureHuntId, count, defaultValue, defaultSymbol);
            return ResponseEntity.status(HttpStatus.CREATED).body(treasures);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/treasures/{treasureId}")
    public ResponseEntity<Treasure> updateTreasure(
            @PathVariable Long treasureId,
            @RequestBody Map<String, Object> request) {

        String name = (String) request.get("name");
        Integer points = request.get("points") != null ? Integer.valueOf(request.get("points").toString()) : null;
        String symbol = (String) request.get("symbol");

        try {
            Treasure updatedTreasure = treasureHuntService.updateTreasure(treasureId, name, points, symbol);
            return ResponseEntity.ok(updatedTreasure);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/treasures/{treasureId}")
    public ResponseEntity<Void> deleteTreasure(@PathVariable Long treasureId) {
        Optional<Treasure> treasureOpt = treasureHuntService.findTreasureById(treasureId);

        if (!treasureOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        treasureHuntService.deleteTreasure(treasureOpt.get());
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{treasureHuntId}/qrcodes")
    public ResponseEntity<List<Map<String, Object>>> generateQRCodes(@PathVariable Long treasureHuntId) {
        try {
            List<Map<String, Object>> qrCodes = treasureHuntService.generateQRCodes(treasureHuntId);
            return ResponseEntity.ok(qrCodes);
        } catch (Exception e) {
            logger.error("Erreur lors de la génération des QR codes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(value = "/treasures/{treasureId}/qrcode-image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getTreasureQRCodeImage(@PathVariable Long treasureId) {
        return (ResponseEntity<byte[]>) treasureHuntService.findTreasureById(treasureId)
                .map(treasure -> {
                    try {
                        byte[] qrCodeImage = treasureHuntService.generateQRCodeImage(treasure.getQrCode(), 300, 300);
                        return ResponseEntity.ok(qrCodeImage);
                    } catch (Exception e) {
                        logger.error("Erreur lors de la génération de l'image QR code", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/scan")
    public ResponseEntity<Map<String, Object>> scanQRCode(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {

        String qrCode = (String) request.get("qrCode");
        Long teamId = request.get("teamId") != null ? Long.valueOf(request.get("teamId").toString()) : null;

        if (qrCode == null || authentication == null) {
            return ResponseEntity.badRequest().build();
        }

        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userOpt.get();
        Long gameSessionId = gameSessionService.getCurrentGameSessionId();

        boolean success = treasureHuntService.recordTreasureFound(qrCode, user.getId(), teamId, gameSessionId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);

        if (success) {
            Optional<Treasure> treasureOpt = treasureHuntService.findTreasureByQrCode(qrCode);
            if (treasureOpt.isPresent()) {
                Treasure treasure = treasureOpt.get();
                response.put("treasureId", treasure.getId());
                response.put("treasureName", treasure.getName());
                response.put("points", treasure.getPoints());
                response.put("symbol", treasure.getSymbol());

                // Récupérer le score actuel de l'utilisateur
                TreasureHuntScenario scenario = treasure.getTreasureHuntScenario();
                Team team = teamId != null ? teamService.findById(teamId).orElse(null) : null;
                response.put("currentScore", treasureHuntService.getOrCreateScore(
                        scenario.getId(), user, team, gameSessionId).getScore());
            }
        } else {
            Optional<Treasure> treasureOpt = treasureHuntService.findTreasureByQrCode(qrCode);
            if (treasureOpt.isPresent()) {
                Treasure treasure = treasureOpt.get();
                TreasureHuntScenario scenario = treasure.getTreasureHuntScenario();

                if (!scenario.getActive()) {
                    response.put("error", "Le scénario n'est pas actif");
                } else if (scenario.getScoresLocked()) {
                    response.put("error", "Les scores sont verrouillés pour ce scénario");
                } else {
                    response.put("error", "Vous avez déjà trouvé ce trésor");
                }
            } else {
                response.put("error", "QR code non reconnu");
            }
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{treasureHuntId}/scores")
    public ResponseEntity<Map<String, Object>> getScoreboard(@PathVariable Long treasureHuntId) {
        try {
            Long gameSessionId = gameSessionService.getCurrentGameSessionId();
            Map<String, Object> scoreboard = treasureHuntService.getScoreboard(treasureHuntId, gameSessionId);
            return ResponseEntity.ok(scoreboard);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération du tableau des scores", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{treasureHuntId}/lock-scores")
    public ResponseEntity<Void> lockScores(@PathVariable Long treasureHuntId, @RequestBody Map<String, Object> request) {
        boolean locked = Boolean.parseBoolean(request.getOrDefault("locked", true).toString());

        try {
            treasureHuntService.lockScores(treasureHuntId, locked);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Erreur lors du verrouillage des scores", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{treasureHuntId}/reset-scores")
    public ResponseEntity<Void> resetScores(@PathVariable Long treasureHuntId) {
        try {
            Long gameSessionId = gameSessionService.getCurrentGameSessionId();
            treasureHuntService.resetScores(treasureHuntId, gameSessionId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Erreur lors de la réinitialisation des scores", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{treasureHuntId}/activate")
    public ResponseEntity<Void> activateScenario(@PathVariable Long treasureHuntId, @RequestBody Map<String, Object> request) {
        boolean active = Boolean.parseBoolean(request.getOrDefault("active", true).toString());

        try {
            treasureHuntService.activateScenario(treasureHuntId, active);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Erreur lors de l'activation du scénario", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{scenarioId}/ensure")
    public ResponseEntity<TreasureHuntScenario> ensureTreasureHuntScenario(@PathVariable Long scenarioId) {
        logger.info("🔎 Vérification du TreasureHuntScenario pour le scénario ID: {}", scenarioId);

        Optional<TreasureHuntScenario> existingScenarioOpt = treasureHuntService.findByScenarioId(scenarioId);

        if (existingScenarioOpt.isPresent()) {
            logger.info("✅ TreasureHuntScenario existant trouvé pour le scénario ID: {}", scenarioId);
            return ResponseEntity.ok(existingScenarioOpt.get());
        }

        logger.info("➕ Aucun TreasureHuntScenario trouvé pour le scénario ID: {}. Recherche du Scenario principal...", scenarioId);

        Optional<Scenario> baseScenarioOpt = scenarioService.findById(scenarioId);

        if (!baseScenarioOpt.isPresent()) {
            logger.error("❌ Aucun Scenario trouvé avec l'ID: {}", scenarioId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Scenario baseScenario = baseScenarioOpt.get();

        TreasureHuntScenario newScenario = new TreasureHuntScenario();
        newScenario.setScenario(baseScenario);
        newScenario.setActive(false);
        newScenario.setScoresLocked(false);
        newScenario.setTotalTreasures(0);
        newScenario.setDefaultValue(50);
        newScenario.setDefaultSymbol("💰");
        newScenario.setSize("SMALL");

        TreasureHuntScenario savedScenario = treasureHuntService.saveTreasureHuntScenario(newScenario);

        logger.info("🎯 Nouveau TreasureHuntScenario créé avec ID interne: {}", savedScenario.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(savedScenario);
    }


}
