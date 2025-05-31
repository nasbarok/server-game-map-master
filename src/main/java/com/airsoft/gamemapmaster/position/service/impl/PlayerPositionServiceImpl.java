package com.airsoft.gamemapmaster.position.service.impl;

import com.airsoft.gamemapmaster.controller.GameSessionController;
import com.airsoft.gamemapmaster.model.ConnectedPlayer;
import com.airsoft.gamemapmaster.model.GameSession;
import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.repository.ConnectedPlayerRepository;
import com.airsoft.gamemapmaster.repository.GameSessionRepository;
import com.airsoft.gamemapmaster.position.dto.GameSessionPositionHistoryDTO;
import com.airsoft.gamemapmaster.position.dto.PlayerPositionDTO;
import com.airsoft.gamemapmaster.position.model.PlayerPosition;
import com.airsoft.gamemapmaster.position.repository.PlayerPositionRepository;
import com.airsoft.gamemapmaster.position.service.PlayerPositionService;
import com.airsoft.gamemapmaster.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Service pour la gestion des positions des joueurs
 */
@Service
public class PlayerPositionServiceImpl implements PlayerPositionService {
    private static final Logger logger = LoggerFactory.getLogger(PlayerPositionServiceImpl.class);

    @Autowired
    private PlayerPositionRepository playerPositionRepository;
    
    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private ConnectedPlayerRepository connectedPlayerRepository;
    @Override
    public PlayerPositionDTO savePosition(PlayerPositionDTO positionDTO) {
        // Si l'horodatage n'est pas fourni, utiliser l'heure actuelle
        if (positionDTO.getTimestamp() == null) {
            positionDTO.setTimestamp(LocalDateTime.now());
        }
        
        // Convertir le DTO en entité
        PlayerPosition position = new PlayerPosition();
        position.setUserId(positionDTO.getUserId());
        position.setGameSessionId(positionDTO.getGameSessionId());
        position.setTeamId(positionDTO.getTeamId());
        position.setLatitude(positionDTO.getLatitude());
        position.setLongitude(positionDTO.getLongitude());
        position.setTimestamp(positionDTO.getTimestamp());

        // Enregistrer la position
        PlayerPosition savedPosition = playerPositionRepository.save(position);

        // Retourner le DTO de la position enregistrée
        return savedPosition.toDTO();
    }

    @Override
    public GameSessionPositionHistoryDTO getPositionHistory(Long gameSessionId) {
        // Récupérer la session de jeu
        GameSession gameSession = gameSessionRepository.findById(gameSessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session de jeu non trouvée avec l'ID: " + gameSessionId));
        
        // Récupérer toutes les positions pour cette session
        List<PlayerPosition> positions = playerPositionRepository.findByGameSessionIdOrderByTimestamp(gameSessionId);

        // Organiser les positions par joueur
        Map<Long, List<PlayerPositionDTO>> playerPositions = new HashMap<>();
        
        for (PlayerPosition position : positions) {
            Long userId = position.getUserId();
            
            if (!playerPositions.containsKey(userId)) {
                playerPositions.put(userId, new ArrayList<>());
            }
            
            playerPositions.get(userId).add(position.toDTO());
        }

        // Créer et retourner l'historique
        return new GameSessionPositionHistoryDTO(
                gameSessionId,
                gameSession.getStartTime(),
                gameSession.getEndTime(),
                playerPositions
        );
    }

    @Override
    public GameSessionPositionHistoryDTO getPlayerPositionHistory(Long gameSessionId, Long userId) {
        // Récupérer la session de jeu
        GameSession gameSession = gameSessionRepository.findById(gameSessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session de jeu non trouvée avec l'ID: " + gameSessionId));

        // Récupérer les positions du joueur
        List<PlayerPosition> positions = playerPositionRepository.findByGameSessionIdAndUserIdOrderByTimestamp(gameSessionId, userId);

        // Créer la structure de données pour l'historique
        Map<Long, List<PlayerPositionDTO>> playerPositions = new HashMap<>();
        List<PlayerPositionDTO> positionDTOs = new ArrayList<>();

        for (PlayerPosition position : positions) {
            positionDTOs.add(position.toDTO());
        }

        playerPositions.put(userId, positionDTOs);

        // Créer et retourner l'historique
        return new GameSessionPositionHistoryDTO(
                gameSessionId,
                gameSession.getStartTime(),
                gameSession.getEndTime(),
                playerPositions
        );
    }

    @Override
    public GameSessionPositionHistoryDTO getTeamPositionHistory(Long gameSessionId, Long teamId) {
        // Récupérer la session de jeu
        GameSession gameSession = gameSessionRepository.findById(gameSessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session de jeu non trouvée avec l'ID: " + gameSessionId));

        // Récupérer les positions de l'équipe
        List<PlayerPosition> positions = playerPositionRepository.findByGameSessionIdAndTeamIdOrderByTimestamp(gameSessionId, teamId);

        // Organiser les positions par joueur
        Map<Long, List<PlayerPositionDTO>> playerPositions = new HashMap<>();

        for (PlayerPosition position : positions) {
            Long userId = position.getUserId();

            if (!playerPositions.containsKey(userId)) {
                playerPositions.put(userId, new ArrayList<>());
            }

            playerPositions.get(userId).add(position.toDTO());
        }
        
        // Créer et retourner l'historique
        return new GameSessionPositionHistoryDTO(
                gameSessionId,
                gameSession.getStartTime(),
                gameSession.getEndTime(),
                playerPositions
        );
    }

    @Override
    public Map<Long, PlayerPositionDTO> getLastKnownPositionsByField(Integer fieldId) {
        // Récupérer toutes les utilisations de positions pour le terrain

        List<ConnectedPlayer> connectedPlayers = connectedPlayerRepository.findByFieldIdAndActiveTrue(Long.valueOf(fieldId));
        if (connectedPlayers.isEmpty()) {
            logger.warn("Aucun joueur connecté pour le terrain avec l'ID: {}", fieldId);
            return new HashMap<>(); // Aucun joueur connecté
        }
        // Organiser les positions par joueur
        Map<Long, PlayerPositionDTO> lastKnownPositions = new HashMap<>();
        for (ConnectedPlayer connectedPlayer : connectedPlayers) {
            Optional<PlayerPosition> positionOpt = playerPositionRepository.findTopByUserIdOrderByTimestampDesc(connectedPlayer.getUser().getId());
            if (positionOpt.isEmpty()) {
                logger.error("Aucune position trouvée pour le joueur avec l'ID: {}", connectedPlayer.getUser().getId());
                continue; // Pas de position pour ce joueur
            }
            PlayerPosition position = positionOpt.get();
            lastKnownPositions.put(connectedPlayer.getUser().getId(), position.toDTO());
        }
        if (lastKnownPositions.isEmpty()) {
            logger.warn("Aucune position trouvée pour les joueurs connectés au terrain avec l'ID: {}", fieldId);
            return new HashMap<>(); // Aucun joueur avec une position connue
        }
        logger.info("Récupération des dernières positions connues pour le terrain avec l'ID: {} nombre de joueurs: {}", fieldId, lastKnownPositions.size());
        return lastKnownPositions;
    }

    public Optional<Instant> getLastSavedTimestamp(Long userId) {
        return playerPositionRepository
                .findTopByUserIdOrderByTimestampDesc(userId)
                .map(pos -> pos.getTimestamp().atZone(ZoneId.systemDefault()).toInstant());
    }
}
