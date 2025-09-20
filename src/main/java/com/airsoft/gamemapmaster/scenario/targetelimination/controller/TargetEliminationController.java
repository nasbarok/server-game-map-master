package com.airsoft.gamemapmaster.scenario.targetelimination.controller;

import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.scenario.targetelimination.model.Elimination;
import com.airsoft.gamemapmaster.scenario.targetelimination.model.PlayerTarget;
import com.airsoft.gamemapmaster.scenario.targetelimination.model.TargetEliminationScenario;
import com.airsoft.gamemapmaster.scenario.targetelimination.service.TargetEliminationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/scenarios/target-elimination")
public class TargetEliminationController {

    @Autowired
    private TargetEliminationService targetEliminationService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Configuration du scénario
    @PostMapping
    public ResponseEntity<TargetEliminationScenario> createScenario(
            @RequestBody TargetEliminationScenario scenario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(targetEliminationService.saveScenario(scenario));
    }

    @GetMapping("/{scenarioId}")
    public ResponseEntity<TargetEliminationScenario> getScenario(@PathVariable Long scenarioId) {
        return targetEliminationService.findByScenarioId(scenarioId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Affectation des cibles
    @PostMapping("/{scenarioId}/assign-targets")
    public ResponseEntity<List<PlayerTarget>> assignTargets(
            @PathVariable Long scenarioId,
            @RequestParam Long gameSessionId) {
        try {
            List<PlayerTarget> targets = targetEliminationService.assignTargetsToPlayers(scenarioId, gameSessionId);
            return ResponseEntity.ok(targets);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Scan QR et élimination
    @PostMapping("/scan")
    public ResponseEntity<Map<String, Object>> scanQRCode(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {

        String qrCode = (String) request.get("qrCode");
        Long gameSessionId = Long.valueOf(request.get("gameSessionId").toString());

        Optional<User> killerOpt = userService.findByUsername(authentication.getName());
        if (!killerOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Elimination> eliminationOpt = targetEliminationService.recordElimination(
                qrCode, killerOpt.get().getId(), gameSessionId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", eliminationOpt.isPresent());

        if (eliminationOpt.isPresent()) {
            Elimination elimination = eliminationOpt.get();

            // Envoi WebSocket
            Long fieldId = gameSessionService.findById(gameSessionId)
                    .map(session -> session.getField().getId())
                    .orElse(null);

            if (fieldId != null) {
                WebSocketMessage wsMessage = TargetEliminationNotification.playerEliminated(elimination);
                messagingTemplate.convertAndSend("/topic/field/" + fieldId, wsMessage);
            }

            response.put("elimination", elimination);
            response.put("points", elimination.getPoints());
        } else {
            response.put("error", "Élimination impossible");
        }

        return ResponseEntity.ok(response);
    }

    // Génération des QR codes
    @GetMapping("/{scenarioId}/qrcodes")
    public ResponseEntity<List<Map<String, Object>>> generateQRCodes(@PathVariable Long scenarioId) {
        try {
            List<Map<String, Object>> qrCodes = targetEliminationService.generateQRCodes(scenarioId);
            return ResponseEntity.ok(qrCodes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Tableau des scores
    @GetMapping("/{scenarioId}/scores")
    public ResponseEntity<Map<String, Object>> getScoreboard(
            @PathVariable Long scenarioId,
            @RequestParam Long gameSessionId) {
        try {
            Map<String, Object> scoreboard = targetEliminationService.getScoreboard(scenarioId, gameSessionId);
            return ResponseEntity.ok(scoreboard);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}