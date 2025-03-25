package com.airsoft.gamemapmaster.scenario.treasurehunt.repository;

import com.airsoft.gamemapmaster.scenario.treasurehunt.model.Treasure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TreasureRepository extends JpaRepository<Treasure, Long> {
    List<Treasure> findByTreasureHuntScenarioId(Long treasureHuntScenarioId);
    Optional<Treasure> findByQrCode(String qrCode);
}
