package com.airsoft.gamemapmaster.scenario.treasurehunt.model;

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
@Table(name = "treasures")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Treasure {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "treasure_hunt_scenario_id")
    private TreasureHuntScenario treasureHuntScenario;
    
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String symbol = "💰";

    private String description;

    @Column(name = "qr_code", nullable = false, unique = true)
    private String qrCode;
    
    private Double latitude;
    
    private Double longitude;
    
    private Integer points;

    @Column(nullable = false)
    private Integer orderNumber;

    @JsonIgnore
    @OneToMany(mappedBy = "treasure", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TreasureFound> treasuresFound = new HashSet<>();
}
