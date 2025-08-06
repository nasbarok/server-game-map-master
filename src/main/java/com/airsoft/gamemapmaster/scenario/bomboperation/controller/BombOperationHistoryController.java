package com.airsoft.gamemapmaster.scenario.bomboperation.controller;

import com.airsoft.gamemapmaster.scenario.bomboperation.dto.BombOperationHistoryDto;
import com.airsoft.gamemapmaster.scenario.bomboperation.dto.BombSiteHistoryDto;
import com.airsoft.gamemapmaster.scenario.bomboperation.service.BombSiteSessionStateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Contrôleur REST pour l'historique et le replay des sessions Bomb Operation
 */
@RestController
@RequestMapping("/api/bomb-operation/history")
@CrossOrigin(origins = "*")
public class BombOperationHistoryController {
    private static final Logger logger = LoggerFactory.getLogger(BombOperationHistoryController.class);

    @Autowired
    private BombSiteSessionStateService bombSiteSessionStateService;

    /**
     * Obtient l'historique complet d'une session pour le replay
     * GET /api/bomb-operation/history/{gameSessionId}
     */
    @GetMapping("/{gameSessionId}")
    public ResponseEntity<BombOperationHistoryDto> getSessionHistory(@PathVariable Long gameSessionId) {
        try {
            BombOperationHistoryDto history = bombSiteSessionStateService.getSessionHistory(gameSessionId);

            ObjectMapper mapper = new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);


            String json = mapper.writeValueAsString(history);
            logger.info("💣 [getSessionHistory] JSON renvoyé pour gameSessionId={}: {}", gameSessionId, json);

            return ResponseEntity.ok(history);
        } catch (Exception e) {
            logger.error("❌ Erreur getSessionHistory pour gameSessionId={}", gameSessionId, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtient l'historique de tous les sites d'une session
     * GET /api/bomb-operation/history/{gameSessionId}/sites
     */
    @GetMapping("/{gameSessionId}/sites")
    public ResponseEntity<List<BombSiteHistoryDto>> getBombSitesHistory(@PathVariable Long gameSessionId) {
        try {
            List<BombSiteHistoryDto> sitesHistory = bombSiteSessionStateService.getBombSitesHistory(gameSessionId);
            return ResponseEntity.ok(sitesHistory);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtient la timeline des événements d'une session
     * GET /api/bomb-operation/history/{gameSessionId}/timeline
     */
    @GetMapping("/{gameSessionId}/timeline")
    public ResponseEntity<List<BombOperationHistoryDto.BombEventDto>> getSessionTimeline(@PathVariable Long gameSessionId) {
        try {
            List<BombOperationHistoryDto.BombEventDto> timeline = bombSiteSessionStateService.getSessionTimeline(gameSessionId);
            return ResponseEntity.ok(timeline);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtient l'état des sites à un moment donné (pour le replay)
     * GET /api/bomb-operation/history/{gameSessionId}/state-at-time?timestamp=2024-01-15T10:30:00
     */
    @GetMapping("/{gameSessionId}/state-at-time")
    public ResponseEntity<List<BombSiteHistoryDto>> getSitesStateAtTime(
            @PathVariable Long gameSessionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime timestamp) {
        try {
            List<BombSiteHistoryDto> sitesState = bombSiteSessionStateService.getSitesStateAtTime(gameSessionId, timestamp);
            return ResponseEntity.ok(sitesState);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

