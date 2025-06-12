package com.airsoft.gamemapmaster.scenario.bomboperation.dto;

import lombok.Data;

/**
 * DTO pour les requêtes de désarmement de bombe.
 * Version simplifiée : contient seulement les informations finales.
 */
@Data
public class BombDisarmedRequestDto {
    
    private Long userId;
    private Long bombSiteId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getBombSiteId() {
        return bombSiteId;
    }

    public void setBombSiteId(Long bombSiteId) {
        this.bombSiteId = bombSiteId;
    }
}

