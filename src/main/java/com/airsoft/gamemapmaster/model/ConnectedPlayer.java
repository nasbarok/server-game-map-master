package com.airsoft.gamemapmaster.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "connected_players", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id"}))
public class ConnectedPlayer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "game_map_id", nullable = false)
    private GameMap gameMap;
    
    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;
    
    @Column(nullable = false)
    private LocalDateTime joinedAt;
    
    @Column(nullable = false)
    private boolean active = true;
    
    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
    }

    @ManyToOne
    @JoinColumn(name = "field_id")
    private Field field;
}
