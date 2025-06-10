package com.airsoft.gamemapmaster.scenario.bomboperation.dto;

import lombok.Data;

/**
 * DTO pour les requêtes de désarmement de bombe.
 * Version simplifiée : contient seulement les informations finales.
 */
@Data
public class BombDisarmedRequestDto {
    
    private Long userId;
    private Long siteId;
    private Double latitude;
    private Double longitude;
}

