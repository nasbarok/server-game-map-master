package com.airsoft.gamemapmaster.scenario.bomboperation.exception;

public class BombOperationException extends RuntimeException {

    public BombOperationException(String message) {
        super(message);
    }

    public BombOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public static class ScenarioNotFoundException extends BombOperationException {
        public ScenarioNotFoundException(Long id) {
            super("Scénario d'Opération Bombe non trouvé avec l'ID: " + id);
        }
    }

    public static class BombSiteNotFoundException extends BombOperationException {
        public BombSiteNotFoundException(Long id) {
            super("Site de bombe non trouvé avec l'ID: " + id);
        }
    }

    public static class SessionNotFoundException extends BombOperationException {
        public SessionNotFoundException(Long id) {
            super("Session d'Opération Bombe non trouvée avec l'ID: " + id);
        }

        public SessionNotFoundException(Long gameSessionId, String type) {
            super("Session d'Opération Bombe non trouvée pour la session de jeu: " + gameSessionId);
        }
    }

    public static class InvalidGameStateException extends BombOperationException {
        public InvalidGameStateException(String currentState, String requiredState) {
            super("État de jeu invalide: " + currentState + ", état requis: " + requiredState);
        }
    }

    public static class PlayerStateNotFoundException extends BombOperationException {
        public PlayerStateNotFoundException(Long sessionId, Long userId) {
            super("État du joueur non trouvé pour la session " + sessionId + " et l'utilisateur " + userId);
        }
    }

    public static class BombAlreadyPlantedException extends BombOperationException {
        public BombAlreadyPlantedException() {
            super("Une bombe est déjà posée dans cette session");
        }
    }

    public static class BombNotPlantedException extends BombOperationException {
        public BombNotPlantedException() {
            super("Aucune bombe n'est posée dans cette session");
        }
    }

    public static class InvalidTeamException extends BombOperationException {
        public InvalidTeamException(String team, String requiredTeam) {
            super("Équipe invalide: " + team + ", équipe requise: " + requiredTeam);
        }
    }

    public static class PlayerNotAliveException extends BombOperationException {
        public PlayerNotAliveException(Long userId) {
            super("Le joueur " + userId + " n'est pas en vie");
        }
    }

    public static class NotInBombSiteException extends BombOperationException {
        public NotInBombSiteException() {
            super("Le joueur n'est pas dans un site de bombe actif");
        }
    }
}
