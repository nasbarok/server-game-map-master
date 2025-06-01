package com.airsoft.gamemapmaster.scenario.bomboperation.repository;

import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombOperationTeamRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BombOperationTeamRoleRepository extends JpaRepository<BombOperationTeamRole, Long> {
    List<BombOperationTeamRole> findByGameSessionId(Long gameSessionId);
    void deleteByGameSessionId(Long gameSessionId);
}