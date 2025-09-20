package com.airsoft.gamemapmaster.scenario.targetelimination.model;

import com.airsoft.gamemapmaster.model.Scenario;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;

@Entity
@Table(name = "target_elimination_scenarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TargetEliminationScenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "scenario_id")
    private Scenario scenario;

    @Column(nullable = false)
    private String size = "SMALL";

    // Configuration du scénario
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameMode mode = GameMode.SOLO; // SOLO, TEAM

    @Column(nullable = false)
    private Boolean friendlyFire = false;

    @Column(nullable = false)
    private Integer pointsPerElimination = 1;

    @Column(nullable = false)
    private Integer cooldownMinutes = 5; // Immunité après élimination

    @Column(nullable = false)
    private Integer maxTargets = 50; // Nombre de QR à générer

    @Column(nullable = false)
    private String announcementTemplate = "{killer} a sorti {victim}";

    // État du scénario
    @Column(nullable = false)
    private Boolean active = false;

    @Column(nullable = false)
    private Boolean scoresLocked = false;

    // Relations
    @JsonIgnore
    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PlayerTarget> playerTargets = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Elimination> eliminations = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TargetEliminationScore> scores = new HashSet<>();

    public enum GameMode {
        SOLO, TEAM
    }
}