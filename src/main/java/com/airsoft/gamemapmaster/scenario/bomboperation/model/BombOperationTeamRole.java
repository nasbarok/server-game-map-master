package com.airsoft.gamemapmaster.scenario.bomboperation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "bomb_operation_team_role")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BombOperationTeamRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_session_id", nullable = false)
    private Long gameSessionId;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "role", nullable = false)
    private String role; // "attack" ou "defense"
}

