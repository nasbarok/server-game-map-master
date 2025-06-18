package com.airsoft.gamemapmaster.position.repository;

import com.airsoft.gamemapmaster.position.model.PlayerPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'accès aux données des positions des joueurs
 */
@Repository
public interface PlayerPositionRepository extends JpaRepository<PlayerPosition, Long> {
    
    /**
     * Trouve toutes les positions pour une session de jeu donnée, triées par horodatage
     * @param gameSessionId ID de la session de jeu
     * @return Liste des positions triées par horodatage
     */
    List<PlayerPosition> findByGameSessionIdOrderByTimestamp(Long gameSessionId);
    
    /**
     * Trouve toutes les positions pour un utilisateur dans une session de jeu donnée
     * @param gameSessionId ID de la session de jeu
     * @param userId ID de l'utilisateur
     * @return Liste des positions de l'utilisateur
     */
    List<PlayerPosition> findByGameSessionIdAndUserIdOrderByTimestamp(Long gameSessionId, Long userId);

    /**
     * Trouve toutes les positions pour une équipe dans une session de jeu donnée
     * @param gameSessionId ID de la session de jeu
     * @param teamId ID de l'équipe
     * @return Liste des positions des membres de l'équipe
     */
    List<PlayerPosition> findByGameSessionIdAndTeamIdOrderByTimestamp(Long gameSessionId, Long teamId);

    @Query("SELECT p FROM PlayerPosition p WHERE p.userId = :userId ORDER BY p.timestamp DESC")
    Optional<PlayerPosition> findListPlayerPosition(@Param("userId") Long userId);

    Optional<PlayerPosition> findTopByUserIdOrderByTimestampDesc(Long id);

    void deleteByGameSessionId(Long id);
}
