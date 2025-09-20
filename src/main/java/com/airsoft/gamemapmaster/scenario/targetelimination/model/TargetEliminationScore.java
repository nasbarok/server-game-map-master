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
@Table(name = "target_elimination_scores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TargetEliminationScore {

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

    @ManyToOne
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSession gameSession;

    @Column(nullable = false)
    private Integer kills = 0;

    @Column(nullable = false)
    private Integer deaths = 0;

    @Column(nullable = false)
    private Integer points = 0;

    @Column(nullable = false)
    private OffsetDateTime lastUpdated;

    // Méthode utilitaire
    public Double getKillDeathRatio() {
        return deaths == 0 ? (double) kills : (double) kills / deaths;
    }
}