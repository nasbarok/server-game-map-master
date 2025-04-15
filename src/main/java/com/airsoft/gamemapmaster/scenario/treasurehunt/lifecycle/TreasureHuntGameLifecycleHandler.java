package com.airsoft.gamemapmaster.scenario.treasurehunt.lifecycle;

import com.airsoft.gamemapmaster.event.GameEndEvent;
import com.airsoft.gamemapmaster.event.GameStartEvent;
import com.airsoft.gamemapmaster.model.Scenario;
import com.airsoft.gamemapmaster.scenario.treasurehunt.service.TreasureHuntService;
import com.airsoft.gamemapmaster.service.ScenarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TreasureHuntGameLifecycleHandler {
    private static final Logger logger = LoggerFactory.getLogger(TreasureHuntGameLifecycleHandler.class);

    @Autowired
    private TreasureHuntService treasureHuntService;

    @Autowired
    private ScenarioService scenarioService;

    /**
     * Gère l'événement de début de partie
     */
    @EventListener
    public void handleGameStart(GameStartEvent event) {
        logger.info("Début de partie détecté: session {}", event.getGameSessionId());

        // Récupérer les scénarios actifs pour cette partie
        List<Scenario> activeScenarios = scenarioService.getActiveScenarios(event.getGameId());

        // Filtrer les scénarios de type chasse au trésor
        List<Long> treasureHuntScenarioIds = activeScenarios.stream()
                .filter(scenario -> "treasure_hunt".equals(scenario.getType()))
                .map(Scenario::getId)
                .collect(Collectors.toList());

        // Activer chaque scénario de chasse au trésor
        for (Long scenarioId : treasureHuntScenarioIds) {
            try {
                treasureHuntService.handleGameStart(scenarioId, event.getGameSessionId());
                logger.info("Scénario de chasse au trésor {} activé pour la session {}",
                        scenarioId, event.getGameSessionId());
            } catch (Exception e) {
                logger.error("Erreur lors de l'activation du scénario de chasse au trésor {}", scenarioId, e);
            }
        }
    }

    /**
     * Gère l'événement de fin de partie
     */
    @EventListener
    public void handleGameEnd(GameEndEvent event) {
        logger.info("Fin de partie détectée: session {}", event.getGameSessionId());

        // Récupérer les scénarios actifs pour cette partie
        List<Scenario> activeScenarios = scenarioService.getActiveScenarios(event.getGameId());

        // Filtrer les scénarios de type chasse au trésor
        List<Long> treasureHuntScenarioIds = activeScenarios.stream()
                .filter(scenario -> "treasure_hunt".equals(scenario.getType()))
                .map(Scenario::getId)
                .collect(Collectors.toList());

        // Désactiver chaque scénario de chasse au trésor
        for (Long scenarioId : treasureHuntScenarioIds) {
            try {
                treasureHuntService.handleGameEnd(scenarioId, event.getGameSessionId());
                logger.info("Scénario de chasse au trésor {} désactivé pour la session {}",
                        scenarioId, event.getGameSessionId());
            } catch (Exception e) {
                logger.error("Erreur lors de la désactivation du scénario de chasse au trésor {}", scenarioId, e);
            }
        }
    }
}
