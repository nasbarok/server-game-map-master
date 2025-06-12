package com.airsoft.gamemapmaster.scenario.bomboperation.repository;

import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombSiteSessionState;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombSiteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des états des sites de bombe dans les sessions
 */
@Repository
public interface BombSiteSessionStateRepository extends JpaRepository<BombSiteSessionState, Long> {
    
    /**
     * Trouve tous les états de sites pour une session de jeu donnée
     */
    List<BombSiteSessionState> findByGameSessionId(Long gameSessionId);
    
    /**
     * Trouve tous les sites actifs pour une session de jeu donnée
     */
    List<BombSiteSessionState> findByGameSessionIdAndStatus(Long gameSessionId, BombSiteStatus status);
    
    /**
     * Trouve tous les sites actifs pour une session de jeu donnée
     */
    @Query("SELECT bsss FROM BombSiteSessionState bsss WHERE bsss.gameSessionId = :gameSessionId AND bsss.status = 'ACTIVE'")
    List<BombSiteSessionState> findActiveSitesByGameSessionId(@Param("gameSessionId") Long gameSessionId);
    
    /**
     * Trouve tous les sites armés pour une session de jeu donnée
     */
    @Query("SELECT bsss FROM BombSiteSessionState bsss WHERE bsss.gameSessionId = :gameSessionId AND bsss.status = 'ARMED'")
    List<BombSiteSessionState> findArmedSitesByGameSessionId(@Param("gameSessionId") Long gameSessionId);
    
    /**
     * Trouve tous les sites explosés pour une session de jeu donnée
     */
    @Query("SELECT bsss FROM BombSiteSessionState bsss WHERE bsss.gameSessionId = :gameSessionId AND bsss.status = 'EXPLODED'")
    List<BombSiteSessionState> findExplodedSitesByGameSessionId(@Param("gameSessionId") Long gameSessionId);
    
    /**
     * Trouve tous les sites désarmés pour une session de jeu donnée
     */
    @Query("SELECT bsss FROM BombSiteSessionState bsss WHERE bsss.gameSessionId = :gameSessionId AND bsss.status = 'DISARMED'")
    List<BombSiteSessionState> findDisarmedSitesByGameSessionId(@Param("gameSessionId") Long gameSessionId);
    
    /**
     * Trouve un site spécifique par session et ID de site original
     */
    Optional<BombSiteSessionState> findByGameSessionIdAndOriginalBombSiteId(Long gameSessionId, Long originalBombSiteId);
    
    /**
     * Trouve tous les sites qui devraient avoir explosé (temps écoulé)
     */
    @Query("SELECT bsss FROM BombSiteSessionState bsss WHERE bsss.status = 'ARMED' AND bsss.expectedExplosionAt < :currentTime")
    List<BombSiteSessionState> findSitesThatShouldHaveExploded(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Compte le nombre de sites par statut pour une session donnée
     */
    @Query("SELECT bsss.status, COUNT(bsss) FROM BombSiteSessionState bsss WHERE bsss.gameSessionId = :gameSessionId GROUP BY bsss.status")
    List<Object[]> countSitesByStatusForSession(@Param("gameSessionId") Long gameSessionId);
    
    /**
     * Trouve tous les sites armés par un joueur spécifique
     */
    List<BombSiteSessionState> findByGameSessionIdAndArmedByUserId(Long gameSessionId, Long userId);
    
    /**
     * Trouve tous les sites désarmés par un joueur spécifique
     */
    List<BombSiteSessionState> findByGameSessionIdAndDisarmedByUserId(Long gameSessionId, Long userId);
    
    /**
     * Supprime tous les états de sites pour une session donnée
     */
    void deleteByGameSessionId(Long gameSessionId);
    
    /**
     * Vérifie si une session a des sites actifs
     */
    @Query("SELECT COUNT(bsss) > 0 FROM BombSiteSessionState bsss WHERE bsss.gameSessionId = :gameSessionId AND bsss.status = 'ACTIVE'")
    boolean hasActiveSites(@Param("gameSessionId") Long gameSessionId);
    
    /**
     * Vérifie si une session a des sites armés
     */
    @Query("SELECT COUNT(bsss) > 0 FROM BombSiteSessionState bsss WHERE bsss.gameSessionId = :gameSessionId AND bsss.status = 'ARMED'")
    boolean hasArmedSites(@Param("gameSessionId") Long gameSessionId);
}

