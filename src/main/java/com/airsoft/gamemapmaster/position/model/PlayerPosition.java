package com.airsoft.gamemapmaster.position.model;

import com.airsoft.gamemapmaster.position.dto.PlayerPositionDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.OffsetDateTime;

/**
 * Entité représentant la position d'un joueur à un moment donné
 */

@Entity
@Table(name = "player_positions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerPosition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "game_session_id", nullable = false)
    private Long gameSessionId;
    
    @Column(name = "team_id")
    private Long teamId;
    
    @Column(nullable = false)
    private Double latitude;
    
    @Column(nullable = false)
    private Double longitude;
    
    @Column(nullable = false)
    private OffsetDateTime timestamp;

    public PlayerPositionDTO toDTO() {
        return new PlayerPositionDTO(
                userId,
                gameSessionId,
                teamId,
                latitude,
                longitude,
                timestamp
        );
    }
}
