package com.airsoft.gamemapmaster.model.DTO;

import com.airsoft.gamemapmaster.model.GameSessionScenario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameSessionScenarioDTO {

    private Long id;

    private Long gameSessionId;
    private Long scenarioId;
    private String scenarioName;
    private String scenarioType;
    private boolean active;
    private boolean isMainScenario;

    public static GameSessionScenarioDTO fromEntity(GameSessionScenario entity) {
        return new GameSessionScenarioDTO(
                entity.getId(),
                entity.getGameSession() != null ? entity.getGameSession().getId() : null,
                entity.getScenario() != null ? entity.getScenario().getId() : null,
                entity.getScenario() != null ? entity.getScenario().getName() : null,
                entity.getScenarioType(),
                entity.getActive(),
                entity.getIsMainScenario()
        );
    }
}
