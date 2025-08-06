package com.airsoft.gamemapmaster.scenario.bomboperation.model;

import com.airsoft.gamemapmaster.scenario.bomboperation.dto.BombOperationSessionDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bomb_operation_sessions")
public class BombOperationSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bomb_operation_scenario_id", nullable = false)
    private BombOperationScenario bombOperationScenario;

    @Column(name = "game_session_id", nullable = false)
    private Long gameSessionId;

    @Column(nullable = false)
    private Integer currentRound = 1;

    @Column(nullable = false)
    private Integer attackTeamScore = 0;

    @Column(nullable = false)
    private Integer defenseTeamScore = 0;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BombOperationState gameState = BombOperationState.WAITING;

    @Column
    private OffsetDateTime roundStartTime;

    @Column
    private OffsetDateTime bombPlantedTime;

    @Column
    private OffsetDateTime defuseStartTime;

    @ElementCollection
    @CollectionTable(name = "bomb_operation_active_sites", joinColumns = @JoinColumn(name = "bomb_operation_session_id"))
    @Column(name = "bomb_site_id")
    private List<Long> activeBombSiteIds = new ArrayList<>();

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime lastUpdated;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        lastUpdated = OffsetDateTime.now(ZoneOffset.UTC);
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public BombOperationSessionDto toDto(Map<Long, String> teamRoles) {
        BombOperationSessionDto dto = new BombOperationSessionDto();
        dto.setId(this.id);
        dto.setBombOperationScenario(this.bombOperationScenario != null ? this.bombOperationScenario.toDto() : null);
        dto.setGameSessionId(this.gameSessionId);
        dto.setCurrentRound(this.currentRound);
        dto.setAttackTeamScore(this.attackTeamScore);
        dto.setDefenseTeamScore(this.defenseTeamScore);
        dto.setGameState(this.gameState != null ? this.gameState.toString() : null);
        dto.setRoundStartTime(this.roundStartTime);
        dto.setBombPlantedTime(this.bombPlantedTime);
        dto.setDefuseStartTime(this.defuseStartTime);
        dto.setTeamRoles(teamRoles);
        return dto;
    }
}