package com.airsoft.gamemapmaster.scenario.bomboperation.model;

/**
 * Énumération représentant les différents états possibles d'un site de bombe
 * dans une session de jeu Bomb Operation.
 * Version simplifiée : seuls les états finaux sont gérés côté backend.
 */
public enum BombSiteStatus {
    /**
     * Site actif et disponible pour l'amorçage
     */
    ACTIVE,
    
    /**
     * Site amorcé avec une bombe active
     */
    ARMED,
    
    /**
     * Site désamorcé avec succès
     */
    DEFUSED,
    
    /**
     * Site où la bombe a explosé
     */
    EXPLODED
}

