package com.airsoft.gamemapmaster.model.DTO;

import com.airsoft.gamemapmaster.model.Field;
import com.airsoft.gamemapmaster.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldDTO {

    private Long id;
    private String name;
    private String description;
    private String address;
    private OffsetDateTime openedAt;
    private OffsetDateTime closedAt;
    private boolean active;

    // Infos basiques du propriétaire
    private OwnerDTO owner;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OwnerDTO {
        private Long id;
        private String username;

        public static OwnerDTO fromEntity(User user) {
            if (user == null) return null;
            return new OwnerDTO(user.getId(), user.getUsername());
        }
    }

    public static FieldDTO fromEntity(Field entity) {
        if (entity == null) return null;
        return new FieldDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getAddress(),
                entity.getOpenedAt(),
                entity.getClosedAt(),
                entity.isActive(),
                OwnerDTO.fromEntity(entity.getOwner())
        );
    }
}
