package com.airsoft.gamemapmaster.scenario.treasurehunt.repository;

import com.airsoft.gamemapmaster.scenario.treasurehunt.model.TreasureFound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TreasureFoundRepository extends JpaRepository<TreasureFound, Long> {
    List<TreasureFound> findByUserId(Long userId);
    List<TreasureFound> findByTeamId(Long teamId);
    List<TreasureFound> findByTreasureId(Long treasureId);
}
