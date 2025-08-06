package com.airsoft.gamemapmaster.position.dto;

import java.time.OffsetDateTime;

public class WebSocketMessageDTO {
    private String type;
    private Long senderId;
    private OffsetDateTime timestamp;
    private PlayerPositionDTO payload;

    // Getters/setters

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public PlayerPositionDTO getPayload() {
        return payload;
    }

    public void setPayload(PlayerPositionDTO payload) {
        this.payload = payload;
    }
}