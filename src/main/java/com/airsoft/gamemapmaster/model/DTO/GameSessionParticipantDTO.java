package com.airsoft.gamemapmaster.model.DTO;

import com.airsoft.gamemapmaster.model.GameSessionParticipant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameSessionParticipantDTO {

    private Long id;

    private Long gameSessionId;
    private Long userId;
    private String username;

    private Long teamId;
    private String teamName;

    private String participantType;

    private LocalDateTime createdAt;
    private LocalDateTime leftAt;

    private Boolean isWinner;
    private Long scoreId;

    public static GameSessionParticipantDTO fromEntity(GameSessionParticipant entity) {
        return new GameSessionParticipantDTO(
                entity.getId(),
                entity.getGameSession() != null ? entity.getGameSession().getId() : null,
                entity.getUser() != null ? entity.getUser().getId() : null,
                entity.getUserUsername(),
                entity.getTeam() != null ? entity.getTeam().getId() : null,
                entity.getTeamName(),
                entity.getParticipantType(),
                entity.getCreatedAt(),
                entity.getLeftAt(),
                entity.getIsWinner(),
                entity.getScoreId()
        );
    }
}
