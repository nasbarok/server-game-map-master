package com.airsoft.gamemapmaster.service.impl;

import com.airsoft.gamemapmaster.model.GameMap;
import com.airsoft.gamemapmaster.model.Scenario;
import com.airsoft.gamemapmaster.repository.ScenarioRepository;
import com.airsoft.gamemapmaster.scenario.bomboperation.service.BombOperationScenarioService;
import com.airsoft.gamemapmaster.scenario.treasurehunt.service.TreasureHuntService;
import com.airsoft.gamemapmaster.service.ScenarioService;
import com.airsoft.gamemapmaster.service.GameMapService;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ScenarioServiceImpl implements ScenarioService {

    @Autowired
    private ScenarioRepository scenarioRepository;
    @Autowired
    @Lazy
    private TreasureHuntService treasureHuntService;
    @Autowired
    private GameMapService gameMapService;
    @Autowired
    @Lazy
    private BombOperationScenarioService bombOperationScenarioService;

    @Override
    public List<Scenario> findAll() {
        return scenarioRepository.findAll();
    }

    @Override
    public Optional<Scenario> findById(Long id) {
        return scenarioRepository.findById(id);
    }

    @Override
    public List<Scenario> findByGameMapId(Long gameMapId) {
        return scenarioRepository.findByGameMapId(gameMapId);
    }

    @Override
    public List<Scenario> findByCreatorId(Long creatorId) {
        return scenarioRepository.findByCreatorId(creatorId);
    }

    @Override
    public List<Scenario> findByType(String type) {
        return scenarioRepository.findByType(type);
    }

    @Override
    public Scenario save(Scenario scenario) {
        return scenarioRepository.save(scenario);
    }

    @Override
    public void deleteById(Long id) {
        // 1. Vérifier s'il existe un BombOperationScenario lié et le supprimer
        bombOperationScenarioService.findByScenarioId(id).ifPresent(bombOperationScenario -> {
            // Suppression des scénarios Opération Bombe liés
            bombOperationScenarioService.deleteBombOperationScenario(bombOperationScenario.getId());
        });

        // 2. Vérifier s'il existe un TreasureHuntScenario lié et le supprimer
        treasureHuntService.findByScenarioId(id).ifPresent(treasureHuntScenario -> {
            treasureHuntService.deleteTreasureHuntScenarioById(treasureHuntScenario.getId());
        });

        // 3. Supprimer le Scenario lui-même
        scenarioRepository.deleteById(id);
    }


    @Override
    @Transactional
    public Optional<Scenario> activateScenario(Long id) {
        return scenarioRepository.findById(id)
                .map(scenario -> {
                    scenario.setActive(true);
                    return scenarioRepository.save(scenario);
                });
    }

    @Override
    @Transactional
    public Optional<Scenario> deactivateScenario(Long id) {
        return scenarioRepository.findById(id)
                .map(scenario -> {
                    scenario.setActive(false);
                    return scenarioRepository.save(scenario);
                });
    }

    @Override
    public List<Scenario> getActiveScenarios(Long gameId) {
        return scenarioRepository.findByGameSessionIdAndActive(gameId, true);
    }

    @Override
    public List<Scenario> findByOwnerId(Long id) {
        return scenarioRepository.findByCreatorId(id); // Assuming the owner is the creator in this context
    }
}
