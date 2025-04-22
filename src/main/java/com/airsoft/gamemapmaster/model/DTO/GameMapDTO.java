package com.airsoft.gamemapmaster.model.DTO;

import com.airsoft.gamemapmaster.model.GameMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameMapDTO {

    private Long id;
    private String name;
    private String description;

    public static GameMapDTO fromEntity(GameMap entity) {
        return new GameMapDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription()
        );
    }
}
