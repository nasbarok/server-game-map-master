package com.airsoft.gamemapmaster.scenario.treasurehunt.model;

import com.airsoft.gamemapmaster.model.Team;
import com.airsoft.gamemapmaster.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "treasure_hunt_scores",
        uniqueConstraints = @UniqueConstraint(columnNames = {"treasure_hunt_scenario_id", "user_id", "game_session_id"}))
public class TreasureHuntScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "treasure_hunt_scenario_id")
    private TreasureHuntScenario treasureHuntScenario;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(name = "game_session_id")
    private Long gameSessionId;

    @Column(nullable = false)
    private Integer score = 0;

    @Column(nullable = false)
    private Integer treasuresFound = 0;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    public void incrementScore(Integer points) {
        this.score += points;
        this.treasuresFound++;
    }
}
