package com.airsoft.gamemapmaster.scenario.bomboperation.dto;

import lombok.Data;
import java.time.OffsetDateTime;

/**
 * DTO pour l'historique d'un site de bombe dans une session
 * Contient toutes les informations nécessaires pour le replay
 */
@Data
public class BombSiteHistoryDto {
    
    private Long id;
    private Long gameSessionId;
    private Long originalBombSiteId;
    private String name;
    private Double latitude;
    private Double longitude;
    private Double radius;
    private String status;
    
    // Timestamps pour la timeline
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime activatedAt;
    private OffsetDateTime armedAt;
    private OffsetDateTime disarmedAt;
    private OffsetDateTime explodedAt;
    
    // Informations sur les joueurs
    private Long armedByUserId;
    private String armedByUserName;
    private Long disarmedByUserId;
    private String disarmedByUserName;
    
    // Informations sur le timer
    private Integer bombTimer;
    private OffsetDateTime expectedExplosionAt;
    
    // Calculs pour le replay
    private Long timeRemainingSeconds;
    private Boolean shouldHaveExploded;
}

