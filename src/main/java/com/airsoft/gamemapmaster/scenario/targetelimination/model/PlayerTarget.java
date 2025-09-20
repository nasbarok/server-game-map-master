package com.airsoft.gamemapmaster.scenario.targetelimination.model;

import com.airsoft.gamemapmaster.model.Team;
import com.airsoft.gamemapmaster.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "player_targets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "scenario_id", nullable = false)
    private TargetEliminationScenario scenario;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private User player;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(nullable = false, unique = true)
    private Integer targetNumber; // Numéro affiché (1, 2, 3...)

    @Column(nullable = false, unique = true)
    private String qrCode; // Code QR unique

    @Column(nullable = false)
    private OffsetDateTime assignedAt;

    @Column
    private OffsetDateTime lastEliminatedAt; // Pour le cooldown

    @Column(nullable = false)
    private Boolean active = true;
}