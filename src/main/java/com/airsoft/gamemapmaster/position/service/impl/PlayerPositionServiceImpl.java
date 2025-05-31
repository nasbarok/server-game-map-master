package com.airsoft.gamemapmaster.position.service.impl;

import com.airsoft.gamemapmaster.model.GameSession;
import com.airsoft.gamemapmaster.repository.GameSessionRepository;
import com.airsoft.gamemapmaster.position.dto.GameSessionPositionHistoryDTO;
import com.airsoft.gamemapmaster.position.dto.PlayerPositionDTO;
import com.airsoft.gamemapmaster.position.model.PlayerPosition;
import com.airsoft.gamemapmaster.position.repository.PlayerPositionRepository;
import com.airsoft.gamemapmaster.position.service.PlayerPositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service pour la gestion des positions des joueurs
 */
@Service
public class PlayerPositionServiceImpl implements PlayerPositionService {
    
    @Autowired
    private PlayerPositionRepository playerPositionRepository;
    
    @Autowired
    private GameSessionRepository gameSessionRepository;

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
}
