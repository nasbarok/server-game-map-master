package com.airsoft.gamemapmaster.model.DTO;

import lombok.Data;

@Data
public class RespondInvitationRequest {
    private boolean accepted;

    // Constructeurs
    public RespondInvitationRequest() {}

    public RespondInvitationRequest(boolean accepted) {
        this.accepted = accepted;
    }
}