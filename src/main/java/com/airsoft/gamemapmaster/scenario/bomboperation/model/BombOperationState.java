package com.airsoft.gamemapmaster.scenario.bomboperation.model;

public enum BombOperationState {
    WAITING,        // En attente du début du round
    ROUND_ACTIVE,   // Round en cours
    BOMB_PLANTED,   // Bombe posée
    DEFUSING,       // Désamorçage en cours
    BOMB_DEFUSED,   // Bombe désamorcée
    BOMB_EXPLODED,  // Bombe explosée
    ROUND_OVER,     // Round terminé
    GAME_OVER       // Partie terminée
}
