package com.airsoft.gamemapmaster.service.impl;

import com.airsoft.gamemapmaster.model.Scenario;
import com.airsoft.gamemapmaster.repository.ScenarioRepository;
import com.airsoft.gamemapmaster.service.ScenarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ScenarioServiceImpl implements ScenarioService {

    @Autowired
    private ScenarioRepository scenarioRepository;

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
}
