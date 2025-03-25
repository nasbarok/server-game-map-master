package com.airsoft.gamemapmaster.repository;

import com.airsoft.gamemapmaster.model.Scenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScenarioRepository extends JpaRepository<Scenario, Long> {
    List<Scenario> findByGameMapId(Long gameMapId);
    List<Scenario> findByCreatorId(Long creatorId);
    List<Scenario> findByType(String type);
}
