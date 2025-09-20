package com.airsoft.gamemapmaster.scenario.targetelimination.model;

import com.airsoft.gamemapmaster.model.GameSession;
import com.airsoft.gamemapmaster.model.Team;
import com.airsoft.gamemapmaster.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "eliminations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Elimination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "scenario_id", nullable = false)
    private TargetEliminationScenario scenario;

    @ManyToOne
    @JoinColumn(name = "killer_id", nullable = false)
    private User killer;

    @ManyToOne
    @JoinColumn(name = "victim_id", nullable = false)
    private User victim;

    @ManyToOne
    @JoinColumn(name = "killer_team_id")
    private Team killerTeam;

    @ManyToOne
    @JoinColumn(name = "victim_team_id")
    private Team victimTeam;

    @ManyToOne
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSession gameSession;

    @Column(nullable = false)
    private Integer points;

    @Column(nullable = false)
    private OffsetDateTime eliminatedAt;

    @Column(nullable = false)
    private String qrCodeScanned;
}