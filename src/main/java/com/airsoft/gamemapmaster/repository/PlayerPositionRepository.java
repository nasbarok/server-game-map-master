package com.airsoft.gamemapmaster.repository;

import com.airsoft.gamemapmaster.model.PlayerPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour l'accès aux données des positions des joueurs
 */
@Repository
public interface PlayerPositionRepository extends JpaRepository<PlayerPosition, Long> {
    
    /**
     * Récupère toutes les positions pour une session de jeu, triées par horodatage
     * 
     * @param gameSessionId Identifiant de la session de jeu
     * @return Liste des positions triées par horodatage
     */
    List<PlayerPosition> findByGameSessionIdOrderByTimestampAsc(Long gameSessionId);
    
    /**
     * Récupère toutes les positions d'un joueur pour une session de jeu, triées par horodatage
     * 
     * @param gameSessionId Identifiant de la session de jeu
     * @param userId Identifiant de l'utilisateur
     * @return Liste des positions du joueur triées par horodatage
     */
    List<PlayerPosition> findByGameSessionIdAndUserIdOrderByTimestampAsc(Long gameSessionId, Long userId);
}
