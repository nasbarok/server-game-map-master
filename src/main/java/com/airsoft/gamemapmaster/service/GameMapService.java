package com.airsoft.gamemapmaster.service;

import com.airsoft.gamemapmaster.model.GameMap;

import java.util.List;
import java.util.Optional;

public interface GameMapService {
    List<GameMap> findAll();
    Optional<GameMap> findById(Long id);
    List<GameMap> findByFieldId(Long fieldId);
    List<GameMap> findByCreatorId(Long creatorId);
    GameMap save(GameMap gameMap);
    void deleteById(Long id);

    List<GameMap> findByOwnerId(Long id);

    Optional<GameMap> findFirstByFieldId(Long fieldId);
}
