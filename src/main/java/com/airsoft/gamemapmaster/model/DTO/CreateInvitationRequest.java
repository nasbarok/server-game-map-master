package com.airsoft.gamemapmaster.model.DTO;

import lombok.Data;

@Data
public class CreateInvitationRequest {
    private Long fieldId;
    private Long targetUserId;

    // Constructeurs
    public CreateInvitationRequest() {}

    public CreateInvitationRequest(Long fieldId, Long targetUserId) {
        this.fieldId = fieldId;
        this.targetUserId = targetUserId;
    }
}