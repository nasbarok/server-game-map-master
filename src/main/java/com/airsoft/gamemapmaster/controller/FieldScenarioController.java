package com.airsoft.gamemapmaster.controller;

import com.airsoft.gamemapmaster.model.DTO.ScenarioDTO;
import com.airsoft.gamemapmaster.model.FieldScenario;
import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.repository.FieldScenarioRepository;
import com.airsoft.gamemapmaster.model.Field;
import com.airsoft.gamemapmaster.model.Scenario;
import com.airsoft.gamemapmaster.repository.FieldRepository;
import com.airsoft.gamemapmaster.repository.ScenarioRepository;
import com.airsoft.gamemapmaster.scenario.treasurehunt.model.TreasureHuntScenario;
import com.airsoft.gamemapmaster.scenario.treasurehunt.service.TreasureHuntService;
import com.airsoft.gamemapmaster.service.UserService;
import com.airsoft.gamemapmaster.websocket.WebSocketMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/fields/{fieldId}/scenarios")
public class FieldScenarioController {

    private static final Logger logger = LoggerFactory.getLogger(FieldScenarioController.class);

    private final FieldScenarioRepository fieldScenarioRepository;
    private final FieldRepository fieldRepository;
    private final ScenarioRepository scenarioRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    private TreasureHuntService treasureHuntService;
    @Autowired
    private UserService userService;

    public FieldScenarioController(FieldScenarioRepository fieldScenarioRepository,
                                   FieldRepository fieldRepository,
                                   ScenarioRepository scenarioRepository,
                                   SimpMessagingTemplate messagingTemplate) {
        this.fieldScenarioRepository = fieldScenarioRepository;
        this.fieldRepository = fieldRepository;
        this.scenarioRepository = scenarioRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping
    public List<Map<String, Object>> updateFieldScenarios(
            @PathVariable Long fieldId,
            @RequestBody List<ScenarioDTO> incomingScenarioDtos,
            Authentication authentication
    ) {
        logger.info("🚀 Requête de mise à jour des scénarios reçue pour terrain {}", fieldId);

        Field field = getFieldById(fieldId);
        User user = userService.findByUsername(authentication.getName()).orElse(null);

        List<FieldScenario> existingFieldScenarios = fieldScenarioRepository.findByFieldId(fieldId);

        boolean same = areScenarioListsIdentical(existingFieldScenarios, incomingScenarioDtos);

        List<FieldScenario> savedScenarios;
        if (same) {
            logger.info("🔍 Les scénarios sont identiques, aucune suppression ni sauvegarde nécessaire.");
            savedScenarios = existingFieldScenarios;
        } else {
            deleteExistingFieldScenarios(fieldId);
            savedScenarios = addNewFieldScenarios(field, incomingScenarioDtos);
        }

        List<Map<String, Object>> scenarioDtosAsMaps = buildScenarioDtos(savedScenarios);

        sendScenarioUpdateMessage(fieldId, scenarioDtosAsMaps, user);

        return scenarioDtosAsMaps;
    }

    private List<Map<String, Object>> buildScenarioDtos(List<FieldScenario> fieldScenarios) {
        List<Map<String, Object>> maps = new ArrayList<>();

        for (FieldScenario fs : fieldScenarios) {
            Scenario scenario = scenarioRepository.findById(fs.getScenario().getId())
                    .orElseThrow(() -> new RuntimeException("Scenario not found (id=" + fs.getScenario().getId() + ")"));
            Optional<TreasureHuntScenario> treasureOpt = treasureHuntService.findByScenarioId(scenario.getId());
            TreasureHuntScenario treasure = treasureOpt.orElse(null);

            ScenarioDTO dto = new ScenarioDTO(scenario, treasure);
            maps.add(dto.toMap());
        }

        logger.info("📋 Conversion des ScenarioDTO en Map terminée, total: {}", maps.size());
        return maps;
    }


    private void sendScenarioUpdateMessage(Long fieldId, List<Map<String, Object>> scenarioDtosAsMaps, User user) {
        logger.info("🚀 Début de l'envoi du message SCENARIO_UPDATE pour le terrain {}", fieldId);

        WebSocketMessage message = new WebSocketMessage(
                "SCENARIO_UPDATE",
                Map.of(
                        "fieldId", fieldId,
                        "scenarioDtos", scenarioDtosAsMaps
                ),
                user.getId(),
                System.currentTimeMillis()
        );

        try {
            messagingTemplate.convertAndSend("/topic/field/" + fieldId, message);
            logger.info("📡 WebSocket envoyé pour mise à jour des scénarios terrain {}", fieldId);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'envoi WebSocket: {}", e.getMessage(), e);
        }
    }

    private boolean areScenarioListsIdentical(List<FieldScenario> existing, List<ScenarioDTO> incoming) {
        if (existing.size() != incoming.size()) return false;

        List<Long> existingIds = new ArrayList<>();
        for (FieldScenario fs : existing) existingIds.add(fs.getScenario().getId());

        List<Long> incomingIds = new ArrayList<>();
        for (ScenarioDTO dto : incoming) incomingIds.add(dto.getScenario().getId());

        existingIds.sort(Long::compareTo);
        incomingIds.sort(Long::compareTo);

        return existingIds.equals(incomingIds);
    }

    private void deleteExistingFieldScenarios(Long fieldId) {
        List<FieldScenario> existing = fieldScenarioRepository.findByFieldId(fieldId);
        fieldScenarioRepository.deleteAll(existing);
        logger.info("✅ Suppression des anciens FieldScenarios pour le terrain {}", fieldId);
    }

    private Field getFieldById(Long fieldId) {
        return fieldRepository.findById(fieldId)
                .orElseThrow(() -> new RuntimeException("Field not found (id=" + fieldId + ")"));
    }

    private List<FieldScenario> addNewFieldScenarios(Field field, List<ScenarioDTO> scenarioDTOList) {
        List<FieldScenario> newFieldScenarios = new ArrayList<>();
        for (ScenarioDTO dto : scenarioDTOList) {
            Scenario scenario = scenarioRepository.findById(dto.getScenario().getId())
                    .orElseThrow(() -> new RuntimeException("Scenario not found (id=" + dto.getScenario().getId() + ")"));

            FieldScenario fs = new FieldScenario();
            fs.setField(field);
            fs.setScenario(scenario);
            fs.setActive(true);
            newFieldScenarios.add(fs);
        }

        List<FieldScenario> saved = fieldScenarioRepository.saveAll(newFieldScenarios);
        logger.info("✅ Sauvegarde de {} nouveaux FieldScenarios", saved.size());
        return saved;
    }

    @PostMapping("/{scenarioId}")
    public FieldScenario addScenarioToField(@PathVariable Long fieldId, @PathVariable Long scenarioId) {
        Field field = getFieldById(fieldId);
        Scenario scenario = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new RuntimeException("Scenario not found"));

        FieldScenario fieldScenario = new FieldScenario();
        fieldScenario.setField(field);
        fieldScenario.setScenario(scenario);
        fieldScenario.setActive(true);

        return fieldScenarioRepository.save(fieldScenario);
    }

    @DeleteMapping("/{fieldScenarioId}")
    public void removeScenarioFromField(@PathVariable Long fieldScenarioId) {
        fieldScenarioRepository.deleteById(fieldScenarioId);
    }

    @GetMapping
    public List<Map<String, Object>> getScenariosForField(@PathVariable Long fieldId) {
        List<FieldScenario> fieldScenarios = fieldScenarioRepository.findByFieldId(fieldId);
        List<Map<String, Object>> scenarioDtosAsMaps = buildScenarioDtos(fieldScenarios);
        return scenarioDtosAsMaps;
    }

}
