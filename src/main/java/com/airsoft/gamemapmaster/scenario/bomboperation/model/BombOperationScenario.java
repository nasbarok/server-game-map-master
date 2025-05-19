package com.airsoft.gamemapmaster.scenario.bomboperation.model;

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
@Table(name = "bomb_operation_scenarios")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BombOperationScenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "scenario_id")
    private Scenario scenario;

    @Column(nullable = false)
    private Integer bombTimer = 45; // Temps avant explosion de la bombe

    @Column(nullable = false)
    private Integer defuseTime = 10; // Temps pour désamorcer

    @Column(nullable = false)
    private Integer activeSites = 2; // Nombre de sites actifs par round

    @Column(nullable = false)
    private String attackTeamName = "Terroriste"; // Nom par défaut de l'équipe d'attaque

    @Column(nullable = false)
    private String defenseTeamName = "Anti-terroriste"; // Nom par défaut de l'équipe de défense

    @Column(nullable = false)
    private Boolean active = false;

    @Column(nullable = false)
    private Boolean showZones = true; // Afficher les zones sur la carte

    @Column(nullable = false)
    private Boolean showPointsOfInterest = true; // Afficher les points d'intérêt sur la carte

    @JsonIgnore
    @OneToMany(mappedBy = "bombOperationScenario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BombSite> bombSites = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "bombOperationScenario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BombOperationScore> scores = new HashSet<>();
}
