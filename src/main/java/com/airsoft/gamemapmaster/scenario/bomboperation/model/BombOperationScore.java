package com.airsoft.gamemapmaster.scenario.bomboperation.model;

import com.airsoft.gamemapmaster.model.Team;
import com.airsoft.gamemapmaster.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bomb_operation_scores",
        uniqueConstraints = @UniqueConstraint(columnNames = {"bomb_operation_scenario_id", "user_id", "game_session_id"}))
public class BombOperationScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bomb_operation_scenario_id")
    private BombOperationScenario bombOperationScenario;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(name = "game_session_id")
    private Long gameSessionId;

    @Column(nullable = false)
    private Integer roundsWon = 0;

    @Column(nullable = false)
    private Integer bombsPlanted = 0;

    @Column(nullable = false)
    private Integer bombsDefused = 0;

    @Column(nullable = false)
    private OffsetDateTime lastUpdated;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        lastUpdated = OffsetDateTime.now(ZoneOffset.UTC);
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = OffsetDateTime.now(ZoneOffset.UTC);
    }
}
