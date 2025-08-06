package com.airsoft.gamemapmaster.position.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

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

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX")
    private OffsetDateTime timestamp;

    public PlayerPositionDTO(Long userId, Long gameSessionId, Long teamId, Double latitude, Double longitude, OffsetDateTime timestamp) {
        this.userId = userId;
        this.gameSessionId = gameSessionId;
        this.teamId = teamId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }
}
