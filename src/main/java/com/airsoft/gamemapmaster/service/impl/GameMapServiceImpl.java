package com.airsoft.gamemapmaster.service.impl;

import com.airsoft.gamemapmaster.model.GameMap;
import com.airsoft.gamemapmaster.repository.GameMapRepository;
import com.airsoft.gamemapmaster.service.GameMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GameMapServiceImpl implements GameMapService {

    @Autowired
    private GameMapRepository gameMapRepository;

    @Override
    public List<GameMap> findAll() {
        return gameMapRepository.findAll();
    }

    @Override
    public Optional<GameMap> findById(Long id) {
        return gameMapRepository.findById(id);
    }

    @Override
    public List<GameMap> findByFieldId(Long fieldId) {
        return gameMapRepository.findByFieldId(fieldId);
    }

    @Override
    public List<GameMap> findByCreatorId(Long creatorId) {
        return gameMapRepository.findByCreatorId(creatorId);
    }

    @Override
    public GameMap save(GameMap gameMap) {
        return gameMapRepository.save(gameMap);
    }

    @Override
    public void deleteById(Long id) {
        gameMapRepository.deleteById(id);
    }

    @Override
    public List<GameMap> findByOwnerId(Long id) {
        return gameMapRepository.findByOwnerId(id);
    }

    @Override
    public Optional<GameMap> findFirstByFieldId(Long fieldId) {
        return gameMapRepository.findFirstByFieldId(fieldId);
    }
}
