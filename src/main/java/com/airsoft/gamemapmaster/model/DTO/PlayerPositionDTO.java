package com.airsoft.gamemapmaster.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO représentant la position d'un joueur
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerPositionDTO {
    
    private Long id;
    private Long userId;
    private Long gameSessionId;
    private Long teamId;
    private Double latitude;
    private Double longitude;
    private String timestamp;
}
