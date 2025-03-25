package com.airsoft.gamemapmaster.repository;

import com.airsoft.gamemapmaster.model.GameMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameMapRepository extends JpaRepository<GameMap, Long> {
    List<GameMap> findByFieldId(Long fieldId);
    List<GameMap> findByCreatorId(Long creatorId);

    List<GameMap> findByOwnerId(Long id);
}
