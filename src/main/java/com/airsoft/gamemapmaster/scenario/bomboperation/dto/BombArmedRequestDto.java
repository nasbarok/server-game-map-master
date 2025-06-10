package com.airsoft.gamemapmaster.scenario.bomboperation.dto;

import lombok.Data;

/**
 * DTO pour les requêtes d'armement de bombe.
 * Version simplifiée : contient seulement les informations finales.
 */
@Data
public class BombArmedRequestDto {
    
    private Long userId;
    private Long siteId;
    private Double latitude;
    private Double longitude;
}

