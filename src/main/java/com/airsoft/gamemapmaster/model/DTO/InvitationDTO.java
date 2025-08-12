package com.airsoft.gamemapmaster.model.DTO;

import com.airsoft.gamemapmaster.model.Invitation;
import com.airsoft.gamemapmaster.model.enums.InvitationStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class InvitationDTO {
    private Long id;
    private Long fieldId;
    private String fieldName;
    private Long senderId;
    private String senderUsername;
    private Long targetUserId;
    private String targetUsername;
    private InvitationStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime respondedAt;

    // Constructeur par défaut
    public InvitationDTO() {}

    // Méthode statique pour créer un DTO depuis une Entity
    public static InvitationDTO fromEntity(Invitation invitation) {
        InvitationDTO dto = new InvitationDTO();
        dto.setId(invitation.getId());
        dto.setFieldId(invitation.getField().getId());
        dto.setFieldName(invitation.getField().getName());
        dto.setSenderId(invitation.getSender().getId());
        dto.setSenderUsername(invitation.getSender().getUsername());
        dto.setTargetUserId(invitation.getTargetUser().getId());
        dto.setTargetUsername(invitation.getTargetUser().getUsername());
        dto.setStatus(invitation.getStatus());
        dto.setCreatedAt(invitation.getCreatedAt());
        dto.setRespondedAt(invitation.getRespondedAt());
        return dto;
    }
}
