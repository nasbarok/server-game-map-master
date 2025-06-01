package com.airsoft.gamemapmaster.scenario.bomboperation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BombOperationTeamRolesDTO {
    private Map<String, String> teamRoles; // Map<teamId, role> où role est "attack" ou "defense"
}
