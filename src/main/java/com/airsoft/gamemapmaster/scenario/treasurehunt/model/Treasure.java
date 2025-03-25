package com.airsoft.gamemapmaster.scenario.treasurehunt.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "treasures")
public class Treasure {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "treasure_hunt_scenario_id")
    private TreasureHuntScenario treasureHuntScenario;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Column(nullable = false, unique = true)
    private String qrCode;
    
    private Double latitude;
    
    private Double longitude;
    
    private Integer points;
    
    @OneToMany(mappedBy = "treasure", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TreasureFound> treasuresFound = new HashSet<>();
}
