package com.airsoft.gamemapmaster.model.DTO;

import com.airsoft.gamemapmaster.model.Team;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamDTO {
    private Long id;
    private String name;
    private List<Map<String, Object>> players;

    public static TeamDTO fromEntity(Team team) {
        TeamDTO dto = new TeamDTO();
        dto.id = team.getId();
        dto.name = team.getName();
        dto.players = team.getMembers().stream().map(user -> {
            Map<String, Object> player = new HashMap<>();
            player.put("id", user.getId());
            player.put("username", user.getUsername());
            return player;
        }).collect(Collectors.toList());
        return dto;
    }
}
