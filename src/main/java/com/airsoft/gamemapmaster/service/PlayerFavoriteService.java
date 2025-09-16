package com.airsoft.gamemapmaster.service;


import com.airsoft.gamemapmaster.model.User;

import java.util.List;

public interface PlayerFavoriteService {

    /**
     * Ajouter un joueur aux favoris
     */
    void addToFavorites(Long hostId, Long playerId);

    /**
     * Retirer un joueur des favoris
     */
    void removeFromFavorites(Long hostId, Long playerId);

    /**
     * Récupérer la liste des IDs des joueurs favoris
     */
    List<Long> getFavoritePlayerIds(Long hostId);

    /**
     * Récupérer les détails des joueurs favoris
     */
    List<User> getFavoritePlayersDetails(Long hostId);

    /**
     * Vérifier si un joueur est en favori
     */
    boolean isFavorite(Long hostId, Long playerId);
}