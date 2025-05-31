package com.airsoft.gamemapmaster.position.dto;

import java.time.LocalDateTime;

public class WebSocketMessageDTO {
    private String type;
    private Long senderId;
    private LocalDateTime timestamp;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public PlayerPositionDTO getPayload() {
        return payload;
    }

    public void setPayload(PlayerPositionDTO payload) {
        this.payload = payload;
    }
}