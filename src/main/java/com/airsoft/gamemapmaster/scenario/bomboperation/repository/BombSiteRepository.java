package com.airsoft.gamemapmaster.scenario.bomboperation.repository;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombSite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BombSiteRepository extends JpaRepository<BombSite, Long> {
    List<BombSite> findByBombOperationScenarioId(Long bombOperationScenarioId);
}
