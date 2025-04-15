package com.airsoft.gamemapmaster.scenario.treasurehunt.model;

import com.airsoft.gamemapmaster.model.Scenario;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "treasure_hunt_scenarios")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TreasureHuntScenario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "scenario_id")
    private Scenario scenario;

    @Column(nullable = false)
    private String size = "SMALL";

    private Integer totalTreasures;
    
    private Integer requiredTreasures;

    private Integer defaultValue = 50;

    private String defaultSymbol = "💰";

    private Boolean scoresLocked = false;

    private Boolean active = false;


    @JsonIgnore
    @OneToMany(mappedBy = "treasureHuntScenario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Treasure> treasures = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "treasureHuntScenario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TreasureHuntScore> scores = new HashSet<>();
}
