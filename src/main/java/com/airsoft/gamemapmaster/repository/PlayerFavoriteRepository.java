package com.airsoft.gamemapmaster.repository;

import com.airsoft.gamemapmaster.model.PlayerFavorite;
import com.airsoft.gamemapmaster.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerFavoriteRepository extends JpaRepository<PlayerFavorite, Long> {

    // Trouver tous les favoris d'un host
    @Query("SELECT pf FROM PlayerFavorite pf WHERE pf.host.id = :hostId")
    List<PlayerFavorite> findByHostId(@Param("hostId") Long hostId);

    // Vérifier si un joueur est en favori pour un host
    @Query("SELECT COUNT(pf) > 0 FROM PlayerFavorite pf WHERE pf.host.id = :hostId AND pf.favoritePlayer.id = :playerId")
    boolean existsByHostIdAndFavoritePlayerId(@Param("hostId") Long hostId, @Param("playerId") Long playerId);

    // Supprimer un favori spécifique
    @Modifying
    @Query("DELETE FROM PlayerFavorite pf WHERE pf.host.id = :hostId AND pf.favoritePlayer.id = :playerId")
    void deleteByHostIdAndFavoritePlayerId(@Param("hostId") Long hostId, @Param("playerId") Long playerId);

    // Récupérer les détails des joueurs favoris
    @Query("SELECT pf.favoritePlayer FROM PlayerFavorite pf WHERE pf.host.id = :hostId ORDER BY pf.createdAt DESC")
    List<User> findFavoritePlayersByHostId(@Param("hostId") Long hostId);
}
