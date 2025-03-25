package com.airsoft.gamemapmaster.scenario.treasurehunt.model;

import com.airsoft.gamemapmaster.model.Scenario;
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
@Table(name = "treasure_hunt_scenarios")
public class TreasureHuntScenario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "scenario_id")
    private Scenario scenario;
    
    private Integer totalTreasures;
    
    private Integer requiredTreasures;
    
    private Boolean teamBased = false;
    
    @OneToMany(mappedBy = "treasureHuntScenario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Treasure> treasures = new HashSet<>();
}
