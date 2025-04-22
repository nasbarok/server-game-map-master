package com.airsoft.gamemapmaster.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "game_session_scenarios")
public class GameSessionScenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "game_session_id")
    @JsonBackReference
    private GameSession gameSession;

    @ManyToOne
    @JoinColumn(name = "scenario_id")
    private Scenario scenario;

    @Column(nullable = false)
    private Boolean active = false;

    @Column(nullable = false)
    private String scenarioType;

    @Column(nullable = false)
    private Boolean isMainScenario = false;

    @PrePersist
    protected void onCreate() {
        if (active == null) {
            active = false;
        }
        if (isMainScenario == null) {
            isMainScenario = false;
        }
        if (scenarioType == null && scenario != null) {
            scenarioType = scenario.getType();
        }
    }
}
