package com.airsoft.gamemapmaster.model.DTO;

import com.airsoft.gamemapmaster.model.Field;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldDTO {

    private Long id;
    private String name;
    private boolean active;

    public static FieldDTO fromEntity(Field entity) {
        return new FieldDTO(
                entity.getId(),
                entity.getName(),
                entity.isActive()
        );
    }
}