package com.airsoft.gamemapmaster.repository;

import com.airsoft.gamemapmaster.model.ConnectedPlayer;
import com.airsoft.gamemapmaster.model.GameMap;
import com.airsoft.gamemapmaster.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConnectedPlayerRepository extends JpaRepository<ConnectedPlayer, Long> {
    
    List<ConnectedPlayer> findByGameMapIdAndActiveTrue(Long gameMapId);
    
    Optional<ConnectedPlayer> findByUserIdAndGameMapIdAndActiveTrue(Long userId, Long gameMapId);
    
    List<ConnectedPlayer> findByUserIdAndActiveTrue(Long userId);
    
    List<ConnectedPlayer> findByTeamIdAndActiveTrue(Long teamId);
    
    boolean existsByUserIdAndGameMapIdAndActiveTrue(Long userId, Long gameMapId);
}
