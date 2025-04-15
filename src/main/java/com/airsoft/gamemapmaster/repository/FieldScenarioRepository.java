package com.airsoft.gamemapmaster.repository;

import com.airsoft.gamemapmaster.model.FieldScenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FieldScenarioRepository extends JpaRepository<FieldScenario, Long> {

    List<FieldScenario> findByFieldId(Long fieldId);
}
