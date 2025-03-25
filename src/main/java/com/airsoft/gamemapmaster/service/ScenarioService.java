package com.airsoft.gamemapmaster.service;

import com.airsoft.gamemapmaster.model.Scenario;

import java.util.List;
import java.util.Optional;

public interface ScenarioService {
    List<Scenario> findAll();
    Optional<Scenario> findById(Long id);
    List<Scenario> findByGameMapId(Long gameMapId);
    List<Scenario> findByCreatorId(Long creatorId);
    List<Scenario> findByType(String type);
    Scenario save(Scenario scenario);
    void deleteById(Long id);
    Optional<Scenario> activateScenario(Long id);
    Optional<Scenario> deactivateScenario(Long id);
}
