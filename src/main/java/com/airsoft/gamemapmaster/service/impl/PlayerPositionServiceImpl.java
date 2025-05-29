package com.airsoft.gamemapmaster.service.impl;

import com.airsoft.gamemapmaster.model.DTO.GameSessionPositionHistoryDTO;
import com.airsoft.gamemapmaster.model.DTO.PlayerPositionDTO;
import com.airsoft.gamemapmaster.model.PlayerPosition;
import com.airsoft.gamemapmaster.repository.PlayerPositionRepository;
import com.airsoft.gamemapmaster.service.PlayerPositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    
    /**
     * Sauvegarde une position de joueur
     * 
     * @param positionDTO DTO contenant les informations de position
     * @return DTO de la position sauvegardée
     */
    public PlayerPositionDTO savePosition(PlayerPositionDTO positionDTO) {
        PlayerPosition position = convertToEntity(positionDTO);
        
        // Si pas d'horodatage fourni, utiliser l'heure actuelle
        if (position.getTimestamp() == null) {
            position.setTimestamp(LocalDateTime.now());
        }
        
        PlayerPosition savedPosition = playerPositionRepository.save(position);
        return convertToDTO(savedPosition);
    }
    
    /**
     * Récupère l'historique des positions pour une session de jeu
     * 
     * @param gameSessionId Identifiant de la session de jeu
     * @return DTO contenant l'historique des positions
     */
    public GameSessionPositionHistoryDTO getPositionHistory(Long gameSessionId) {
        List<PlayerPosition> positions = playerPositionRepository.findByGameSessionIdOrderByTimestampAsc(gameSessionId);
        
        Map<Long, List<PlayerPositionDTO>> playerPositions = new HashMap<>();
        
        for (PlayerPosition position : positions) {
            Long userId = position.getUserId();
            
            if (!playerPositions.containsKey(userId)) {
                playerPositions.put(userId, new ArrayList<>());
            }
            
            playerPositions.get(userId).add(convertToDTO(position));
        }
        
        GameSessionPositionHistoryDTO historyDTO = new GameSessionPositionHistoryDTO();
        historyDTO.setGameSessionId(gameSessionId);
        historyDTO.setPlayerPositions(playerPositions);
        
        return historyDTO;
    }
    
    /**
     * Convertit un DTO en entité
     * 
     * @param dto DTO à convertir
     * @return Entité correspondante
     */
    private PlayerPosition convertToEntity(PlayerPositionDTO dto) {
        PlayerPosition entity = new PlayerPosition();
        entity.setId(dto.getId());
        entity.setUserId(dto.getUserId());
        entity.setGameSessionId(dto.getGameSessionId());
        entity.setTeamId(dto.getTeamId());
        entity.setLatitude(dto.getLatitude());
        entity.setLongitude(dto.getLongitude());
        
        if (dto.getTimestamp() != null) {
            entity.setTimestamp(LocalDateTime.parse(dto.getTimestamp()));
        }
        
        return entity;
    }
    
    /**
     * Convertit une entité en DTO
     * 
     * @param entity Entité à convertir
     * @return DTO correspondant
     */
    private PlayerPositionDTO convertToDTO(PlayerPosition entity) {
        PlayerPositionDTO dto = new PlayerPositionDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setGameSessionId(entity.getGameSessionId());
        dto.setTeamId(entity.getTeamId());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setTimestamp(entity.getTimestamp().toString());
        
        return dto;
    }
}
