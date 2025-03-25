package com.airsoft.gamemapmaster.scenario.treasurehunt.service.impl;

import com.airsoft.gamemapmaster.scenario.treasurehunt.model.Treasure;
import com.airsoft.gamemapmaster.scenario.treasurehunt.model.TreasureFound;
import com.airsoft.gamemapmaster.scenario.treasurehunt.model.TreasureHuntScenario;
import com.airsoft.gamemapmaster.scenario.treasurehunt.repository.TreasureFoundRepository;
import com.airsoft.gamemapmaster.scenario.treasurehunt.repository.TreasureHuntScenarioRepository;
import com.airsoft.gamemapmaster.scenario.treasurehunt.repository.TreasureRepository;
import com.airsoft.gamemapmaster.scenario.treasurehunt.service.TreasureHuntService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TreasureHuntServiceImpl implements TreasureHuntService {

    @Autowired
    private TreasureHuntScenarioRepository treasureHuntScenarioRepository;
    
    @Autowired
    private TreasureRepository treasureRepository;
    
    @Autowired
    private TreasureFoundRepository treasureFoundRepository;

    @Override
    @Transactional
    public TreasureHuntScenario saveTreasureHuntScenario(TreasureHuntScenario treasureHuntScenario) {
        return treasureHuntScenarioRepository.save(treasureHuntScenario);
    }

    @Override
    public Optional<TreasureHuntScenario> findById(Long id) {
        return treasureHuntScenarioRepository.findById(id);
    }

    @Override
    public Optional<TreasureHuntScenario> findByScenarioId(Long scenarioId) {
        return treasureHuntScenarioRepository.findByScenarioId(scenarioId);
    }

    @Override
    @Transactional
    public Treasure saveTreasure(Treasure treasure) {
        return treasureRepository.save(treasure);
    }

    @Override
    public Optional<Treasure> findTreasureById(Long id) {
        return treasureRepository.findById(id);
    }

    @Override
    public List<Treasure> findTreasuresByTreasureHuntId(Long treasureHuntId) {
        return treasureRepository.findByTreasureHuntScenarioId(treasureHuntId);
    }

    @Override
    public Optional<Treasure> findTreasureByQrCode(String qrCode) {
        return treasureRepository.findByQrCode(qrCode);
    }

    @Override
    @Transactional
    public TreasureFound saveTreasureFound(TreasureFound treasureFound) {
        return treasureFoundRepository.save(treasureFound);
    }

    @Override
    public List<TreasureFound> findTreasuresFoundByUserId(Long userId) {
        return treasureFoundRepository.findByUserId(userId);
    }

    @Override
    public List<TreasureFound> findTreasuresFoundByTeamId(Long teamId) {
        return treasureFoundRepository.findByTeamId(teamId);
    }

    @Override
    public List<TreasureFound> findTreasuresFoundByTreasureId(Long treasureId) {
        return treasureFoundRepository.findByTreasureId(treasureId);
    }
}
