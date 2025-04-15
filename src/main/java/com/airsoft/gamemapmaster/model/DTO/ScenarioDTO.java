package com.airsoft.gamemapmaster.model.DTO;

import com.airsoft.gamemapmaster.model.Scenario;
import com.airsoft.gamemapmaster.scenario.treasurehunt.model.TreasureHuntScenario;

import java.util.HashMap;
import java.util.Map;

public class ScenarioDTO {
    private Scenario scenario;
    private TreasureHuntScenario treasureHuntScenario;

    public ScenarioDTO(Scenario scenario, TreasureHuntScenario treasureHuntScenario) {
        this.scenario = scenario;
        this.treasureHuntScenario = treasureHuntScenario;
    }

    public Scenario getScenario() {
        return scenario;
    }

    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
    }

    public TreasureHuntScenario getTreasureHuntScenario() {
        return treasureHuntScenario;
    }

    public void setTreasureHuntScenario(TreasureHuntScenario treasureHuntScenario) {
        this.treasureHuntScenario = treasureHuntScenario;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> scenarioPart = new HashMap<>();
        scenarioPart.put("id", scenario.getId());
        scenarioPart.put("name", scenario.getName());
        scenarioPart.put("description", scenario.getDescription());
        scenarioPart.put("type", scenario.getType());
        scenarioPart.put("maxPlayers", scenario.getMaxPlayers());

        Map<String, Object> treasureHuntPart = null;
        if (treasureHuntScenario != null) {
            treasureHuntPart = new HashMap<>();
            treasureHuntPart.put("id", treasureHuntScenario.getId());
            treasureHuntPart.put("scenarioId", treasureHuntScenario.getScenario().getId());
            treasureHuntPart.put("size", treasureHuntScenario.getSize());
            treasureHuntPart.put("totalTreasures", treasureHuntScenario.getTotalTreasures());
            treasureHuntPart.put("requiredTreasures", treasureHuntScenario.getRequiredTreasures());
            treasureHuntPart.put("defaultValue", treasureHuntScenario.getDefaultValue());
            treasureHuntPart.put("defaultSymbol", treasureHuntScenario.getDefaultSymbol());
            treasureHuntPart.put("scoresLocked", treasureHuntScenario.getScoresLocked());
            treasureHuntPart.put("active", treasureHuntScenario.getActive());
        }

        Map<String, Object> dto = new HashMap<>();
        dto.put("scenario", scenarioPart);
        dto.put("treasureHuntScenario", treasureHuntPart);

        return dto;
    }

    public static ScenarioDTO from(Scenario scenario, TreasureHuntScenario treasureHuntScenario) {
        return new ScenarioDTO(scenario, treasureHuntScenario);
    }
}
