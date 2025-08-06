package com.airsoft.gamemapmaster.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "scenarios")
public class Scenario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "game_map_id")
    private GameMap gameMap;
    
    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;

    @ManyToOne
    @JoinColumn(name = "game_session_id")
    private GameSession gameSession;

    @Column(nullable = false)
    private String type; // Type de scénario (ex: "treasure_hunt", "capture_flag", etc.)
    
    private OffsetDateTime startTime;
    
    private OffsetDateTime endTime;
    
    private Integer maxPlayers;
    
    @Column(nullable = false)
    private boolean active = false;
}
