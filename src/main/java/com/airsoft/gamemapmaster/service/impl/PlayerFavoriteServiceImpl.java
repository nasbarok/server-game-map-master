package com.airsoft.gamemapmaster.service.impl;
import com.airsoft.gamemapmaster.model.PlayerFavorite;
import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.repository.PlayerFavoriteRepository;
import com.airsoft.gamemapmaster.repository.UserRepository;
import com.airsoft.gamemapmaster.service.PlayerFavoriteService;
import com.airsoft.gamemapmaster.service.ScenarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlayerFavoriteServiceImpl implements PlayerFavoriteService {

    @Autowired
    private PlayerFavoriteRepository playerFavoriteRepository;

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(PlayerFavoriteServiceImpl.class);

    /**
     * Ajouter un joueur aux favoris
     */
    @Override
    public void addToFavorites(Long hostId, Long playerId) {
        logger.info("🌟 Ajout du joueur {} aux favoris de l'host {}", playerId, hostId);

        // Vérifier que l'host et le joueur existent
        User host = userRepository.findById(hostId)
                .orElseThrow(() -> new EntityNotFoundException("Host non trouvé avec l'ID: " + hostId));

        User player = userRepository.findById(playerId)
                .orElseThrow(() -> new EntityNotFoundException("Joueur non trouvé avec l'ID: " + playerId));

        // Vérifier que l'host ne se marque pas lui-même en favori
        if (hostId.equals(playerId)) {
            throw new IllegalArgumentException("Un host ne peut pas se marquer lui-même en favori");
        }

        // Vérifier si déjà en favori
        if (playerFavoriteRepository.existsByHostIdAndFavoritePlayerId(hostId, playerId)) {
            logger.warn("⚠️ Le joueur {} est déjà en favori pour l'host {}", playerId, hostId);
            return; // Pas d'erreur, juste ignorer
        }

        // Créer et sauvegarder le favori
        PlayerFavorite favorite = new PlayerFavorite(host, player);
        playerFavoriteRepository.save(favorite);

        logger.info("✅ Joueur {} ajouté aux favoris de l'host {}", playerId, hostId);
    }

    /**
     * Retirer un joueur des favoris
     */
    @Override
    public void removeFromFavorites(Long hostId, Long playerId) {
        logger.info("🗑️ Suppression du joueur {} des favoris de l'host {}", playerId, hostId);

        playerFavoriteRepository.deleteByHostIdAndFavoritePlayerId(hostId, playerId);

        logger.info("✅ Joueur {} retiré des favoris de l'host {}", playerId, hostId);
    }

    /**
     * Récupérer la liste des IDs des joueurs favoris
     */
    @Override
    public List<Long> getFavoritePlayerIds(Long hostId) {
        logger.debug("📋 Récupération des IDs favoris pour l'host {}", hostId);

        return playerFavoriteRepository.findByHostId(hostId)
                .stream()
                .map(pf -> pf.getFavoritePlayer().getId())
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les détails des joueurs favoris
     */
    @Override
    public List<User> getFavoritePlayersDetails(Long hostId) {
        logger.debug("📋 Récupération des détails favoris pour l'host {}", hostId);

        return playerFavoriteRepository.findFavoritePlayersByHostId(hostId);
    }

    /**
     * Vérifier si un joueur est en favori
     */
    @Override
    public boolean isFavorite(Long hostId, Long playerId) {
        return playerFavoriteRepository.existsByHostIdAndFavoritePlayerId(hostId, playerId);
    }
}
