package com.airsoft.gamemapmaster.scenario.bomboperation.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entité représentant l'état d'un site de bombe dans une session de jeu
 * Clone des BombSites sélectionnés aléatoirement pour une session spécifique
 */
@Entity
@Table(name = "bomb_site_session_states")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BombSiteSessionState {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    
    /**
     * ID de la session de jeu associée
     */
    @Column(nullable = false)
    private Long gameSessionId;
    
    /**
     * ID du BombSite original dont ceci est un clone
     */
    @Column(nullable = false)
    private Long originalBombSiteId;
    
    /**
     * Nom du site de bombe (copié depuis BombSite original)
     */
    @Column(nullable = false)
    private String name;
    
    /**
     * Latitude du site (copiée depuis BombSite original)
     */
    @Column(nullable = false)
    private Double latitude;
    
    /**
     * Longitude du site (copiée depuis BombSite original)
     */
    @Column(nullable = false)
    private Double longitude;
    
    /**
     * Rayon d'action en mètres (copié depuis BombSite original)
     */
    @Column(nullable = false)
    private Double radius;
    
    /**
     * État actuel du site dans cette session
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BombSiteStatus status = BombSiteStatus.INACTIVE;
    
    /**
     * Timestamp de création de cet état
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * Timestamp de la dernière mise à jour de l'état
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Timestamp d'activation du site (quand il devient ACTIVE)
     */
    private LocalDateTime activatedAt;
    
    /**
     * Timestamp d'armement (quand il devient ARMED)
     */
    private LocalDateTime armedAt;
    
    /**
     * Timestamp de désarmement (quand il devient DISARMED)
     */
    private LocalDateTime disarmedAt;
    
    /**
     * Timestamp d'explosion (quand il devient EXPLODED)
     */
    private LocalDateTime explodedAt;
    
    /**
     * ID du joueur qui a armé la bombe (si applicable)
     */
    private Long armedByUserId;
    
    /**
     * ID du joueur qui a désarmé la bombe (si applicable)
     */
    private Long disarmedByUserId;
    
    /**
     * Durée du timer de la bombe en secondes (au moment de l'armement)
     */
    private Integer bombTimer;
    
    /**
     * Timestamp prévu d'explosion (armedAt + bombTimer)
     */
    private LocalDateTime expectedExplosionAt;
    
    /**
     * Constructeur par défaut
     */
    public BombSiteSessionState() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Constructeur pour créer un clone depuis un BombSite
     */
    public BombSiteSessionState(Long gameSessionId, BombSite originalBombSite) {
        this();
        this.gameSessionId = gameSessionId;
        this.originalBombSiteId = originalBombSite.getId();
        this.name = originalBombSite.getName();
        this.latitude = originalBombSite.getLatitude();
        this.longitude = originalBombSite.getLongitude();
        this.radius = originalBombSite.getRadius();
    }
    
    /**
     * Met à jour le timestamp de dernière modification
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Active le site de bombe
     */
    public void activate() {
        this.status = BombSiteStatus.ACTIVE;
        this.activatedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Arme la bombe sur ce site
     */
    public void arm(Long userId, Integer bombTimerSeconds,LocalDateTime armedAt) {
        this.status = BombSiteStatus.ARMED;
        this.armedAt = armedAt;
        this.armedByUserId = userId;
        this.bombTimer = bombTimerSeconds;
        this.expectedExplosionAt = this.armedAt.plusSeconds(bombTimerSeconds);
        this.updatedAt = armedAt;
    }
    
    /**
     * Désarme la bombe sur ce site
     */
    public void disarm(Long userId,LocalDateTime disarmedAt) {
        this.status = BombSiteStatus.DISARMED;
        this.disarmedAt = disarmedAt;
        this.disarmedByUserId = userId;
        this.updatedAt = disarmedAt;
    }
    
    /**
     * Fait exploser la bombe sur ce site
     */
    public void explode() {
        this.status = BombSiteStatus.EXPLODED;
        this.explodedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Désactive le site (retour à INACTIVE)
     */
    public void deactivate() {
        this.status = BombSiteStatus.INACTIVE;
        this.activatedAt = null;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Vérifie si le site est actif (disponible pour armement)
     */
    public boolean isActive() {
        return this.status == BombSiteStatus.ACTIVE;
    }
    
    /**
     * Vérifie si le site est armé
     */
    public boolean isArmed() {
        return this.status == BombSiteStatus.ARMED;
    }
    
    /**
     * Vérifie si le site est désarmé
     */
    public boolean isDisarmed() {
        return this.status == BombSiteStatus.DISARMED;
    }
    
    /**
     * Vérifie si le site a explosé
     */
    public boolean isExploded() {
        return this.status == BombSiteStatus.EXPLODED;
    }
    
    /**
     * Vérifie si la bombe devrait avoir explosé (temps écoulé)
     */
    public boolean shouldHaveExploded() {
        return isArmed() && 
               expectedExplosionAt != null && 
               LocalDateTime.now().isAfter(expectedExplosionAt);
    }
    
    /**
     * Calcule le temps restant avant explosion (en secondes)
     * Retourne 0 si pas armé ou déjà explosé
     */
    public long getTimeRemainingSeconds() {
        if (!isArmed() || expectedExplosionAt == null) {
            return 0;
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(expectedExplosionAt)) {
            return 0;
        }
        
        return java.time.Duration.between(now, expectedExplosionAt).getSeconds();
    }
}

