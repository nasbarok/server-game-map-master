package com.airsoft.gamemapmaster.model.DTO;

import com.airsoft.gamemapmaster.controller.GameSessionController;
import com.airsoft.gamemapmaster.model.GameSession;
import com.airsoft.gamemapmaster.model.GameSessionParticipant;
import com.airsoft.gamemapmaster.model.GameSessionScenario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameSessionDTO {
    private static final Logger logger = LoggerFactory.getLogger(GameSessionDTO.class);

    private Long id;
    private Integer durationMinutes;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private boolean active;

    private GameMapDTO gameMap;
    private FieldDTO field;

    private List<GameSessionParticipantDTO> participants;
    private List<GameSessionScenarioDTO> scenarios;

    public static GameSessionDTO fromEntity(GameSession entity) {
        if (entity == null) {
            System.err.println("❌ GameSession entity est null !");
            return null;
        }
        List<GameSessionParticipantDTO> participantDTOs = new ArrayList<>();
        if (entity.getParticipants() != null) {
            for (GameSessionParticipant participant : entity.getParticipants()) {
                participantDTOs.add(GameSessionParticipantDTO.fromEntity(participant));
            }
        }

        List<GameSessionScenarioDTO> scenarioDTOs = new ArrayList<>();
        if (entity.getScenarios() != null) {
            for (GameSessionScenario scenario : entity.getScenarios()) {
                scenarioDTOs.add(GameSessionScenarioDTO.fromEntity(scenario));
            }
        }

        return new GameSessionDTO(
                entity.getId(),
                entity.getDurationMinutes(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getActive(),
                entity.getGameMap() != null ? GameMapDTO.fromEntity(entity.getGameMap()) : null,
                entity.getField() != null ? FieldDTO.fromEntity(entity.getField()) : null,
                participantDTOs,
                scenarioDTOs
        );
    }
}
