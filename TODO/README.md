# Fichiers ajoutés ou modifiés

Ce document liste tous les fichiers qui ont été ajoutés ou modifiés pour implémenter les fonctionnalités de gestion des joueurs connectés et du lobby.

## Modèles

- `src/main/java/com/airsoft/gamemapmaster/model/ConnectedPlayer.java` - Nouveau modèle pour représenter les joueurs connectés à une carte

## Repositories

- `src/main/java/com/airsoft/gamemapmaster/repository/ConnectedPlayerRepository.java` - Repository pour le modèle ConnectedPlayer
- `src/main/java/com/airsoft/gamemapmaster/repository/InvitationRepository.java` - Repository pour le modèle Invitation existant

## Services

- `src/main/java/com/airsoft/gamemapmaster/service/ConnectedPlayerService.java` - Interface de service pour la gestion des joueurs connectés
- `src/main/java/com/airsoft/gamemapmaster/service/InvitationService.java` - Interface de service pour la gestion des invitations
- `src/main/java/com/airsoft/gamemapmaster/service/impl/ConnectedPlayerServiceImpl.java` - Implémentation du service ConnectedPlayerService
- `src/main/java/com/airsoft/gamemapmaster/service/impl/InvitationServiceImpl.java` - Implémentation du service InvitationService

## Contrôleurs

- `src/main/java/com/airsoft/gamemapmaster/controller/PlayerConnectionController.java` - Contrôleur pour la gestion des joueurs connectés
- `src/main/java/com/airsoft/gamemapmaster/controller/InvitationController.java` - Contrôleur pour la gestion des invitations
- `src/main/java/com/airsoft/gamemapmaster/controller/GameSessionController.java` - Contrôleur pour la gestion des sessions de jeu

## Tests

- `src/test/java/com/airsoft/gamemapmaster/controller/PlayerConnectionControllerTest.java` - Tests pour PlayerConnectionController
- `src/test/java/com/airsoft/gamemapmaster/controller/InvitationControllerTest.java` - Tests pour InvitationController
- `src/test/java/com/airsoft/gamemapmaster/controller/GameSessionControllerTest.java` - Tests pour GameSessionController

## Documentation

- `API_DOCUMENTATION.md` - Documentation complète des nouveaux endpoints
- `IMPLEMENTATION_REPORT.md` - Rapport d'implémentation détaillant le travail effectué
