# Documentation des API pour la gestion des joueurs connectés et du lobby

## Introduction

Cette documentation décrit les nouveaux endpoints implémentés pour gérer les fonctionnalités de lobby et de gestion des joueurs connectés dans l'application Game Map Master.

## Modèles de données

### ConnectedPlayer

Représente un joueur connecté à une carte de jeu.

```java
{
  "id": Long,
  "user": User,
  "gameMap": GameMap,
  "team": Team,
  "joinedAt": LocalDateTime,
  "active": boolean
}
```

## Endpoints pour la gestion des joueurs connectés

### Rejoindre une carte

**Endpoint:** `POST /api/maps/{mapId}/join`

**Description:** Permet à un joueur de rejoindre une carte de jeu.

**Paramètres:**
- `mapId` (path): ID de la carte à rejoindre
- `teamId` (query, optionnel): ID de l'équipe à rejoindre

**Réponses:**
- `201 Created`: Joueur connecté avec succès
- `401 Unauthorized`: Utilisateur non authentifié
- `404 Not Found`: Carte non trouvée
- `409 Conflict`: Joueur déjà connecté à cette carte
- `400 Bad Request`: Erreur dans la requête

### Quitter une carte

**Endpoint:** `POST /api/maps/{mapId}/leave`

**Description:** Permet à un joueur de quitter une carte de jeu.

**Paramètres:**
- `mapId` (path): ID de la carte à quitter

**Réponses:**
- `200 OK`: Joueur déconnecté avec succès
- `401 Unauthorized`: Utilisateur non authentifié
- `404 Not Found`: Joueur non connecté à cette carte

### Lister les joueurs sur une carte

**Endpoint:** `GET /api/maps/{mapId}/players`

**Description:** Récupère la liste des joueurs connectés à une carte.

**Paramètres:**
- `mapId` (path): ID de la carte

**Réponses:**
- `200 OK`: Liste des joueurs connectés
- `404 Not Found`: Carte non trouvée

### Fermer une carte

**Endpoint:** `POST /api/maps/{mapId}/close`

**Description:** Ferme une carte et déconnecte tous les joueurs. Réservé au propriétaire de la carte.

**Paramètres:**
- `mapId` (path): ID de la carte à fermer

**Réponses:**
- `200 OK`: Carte fermée avec succès
- `401 Unauthorized`: Utilisateur non authentifié
- `403 Forbidden`: Utilisateur non autorisé (pas le propriétaire)
- `404 Not Found`: Carte non trouvée

### Assigner un joueur à une équipe

**Endpoint:** `POST /api/maps/{mapId}/players/{userId}/team/{teamId}`

**Description:** Assigne un joueur connecté à une équipe. Réservé au propriétaire de la carte.

**Paramètres:**
- `mapId` (path): ID de la carte
- `userId` (path): ID du joueur
- `teamId` (path): ID de l'équipe

**Réponses:**
- `200 OK`: Joueur assigné avec succès
- `401 Unauthorized`: Utilisateur non authentifié
- `403 Forbidden`: Utilisateur non autorisé (pas le propriétaire)
- `404 Not Found`: Carte, joueur ou équipe non trouvé

## Endpoints pour la gestion des invitations

### Créer une invitation

**Endpoint:** `POST /api/invitations`

**Description:** Crée une invitation pour un utilisateur à rejoindre un scénario.

**Paramètres:**
- `scenarioId` (query): ID du scénario
- `userId` (query): ID de l'utilisateur à inviter
- `teamId` (query, optionnel): ID de l'équipe

**Réponses:**
- `201 Created`: Invitation créée avec succès
- `401 Unauthorized`: Utilisateur non authentifié
- `403 Forbidden`: Utilisateur non autorisé (pas le créateur du scénario)
- `404 Not Found`: Scénario non trouvé
- `400 Bad Request`: Erreur dans la requête

### Récupérer mes invitations

**Endpoint:** `GET /api/invitations/me`

**Description:** Récupère toutes les invitations pour l'utilisateur connecté.

**Réponses:**
- `200 OK`: Liste des invitations
- `401 Unauthorized`: Utilisateur non authentifié

### Récupérer mes invitations en attente

**Endpoint:** `GET /api/invitations/me/pending`

**Description:** Récupère toutes les invitations en attente pour l'utilisateur connecté.

**Réponses:**
- `200 OK`: Liste des invitations en attente
- `401 Unauthorized`: Utilisateur non authentifié

### Récupérer les invitations pour un scénario

**Endpoint:** `GET /api/invitations/scenario/{scenarioId}`

**Description:** Récupère toutes les invitations pour un scénario. Réservé au créateur du scénario.

**Paramètres:**
- `scenarioId` (path): ID du scénario

**Réponses:**
- `200 OK`: Liste des invitations
- `401 Unauthorized`: Utilisateur non authentifié
- `403 Forbidden`: Utilisateur non autorisé (pas le créateur du scénario)
- `404 Not Found`: Scénario non trouvé

### Accepter une invitation

**Endpoint:** `POST /api/invitations/{invitationId}/accept`

**Description:** Accepte une invitation.

**Paramètres:**
- `invitationId` (path): ID de l'invitation

**Réponses:**
- `200 OK`: Invitation acceptée avec succès
- `401 Unauthorized`: Utilisateur non authentifié
- `404 Not Found`: Invitation non trouvée ou déjà traitée

### Refuser une invitation

**Endpoint:** `POST /api/invitations/{invitationId}/decline`

**Description:** Refuse une invitation.

**Paramètres:**
- `invitationId` (path): ID de l'invitation

**Réponses:**
- `200 OK`: Invitation refusée avec succès
- `401 Unauthorized`: Utilisateur non authentifié
- `404 Not Found`: Invitation non trouvée ou déjà traitée

### Annuler une invitation

**Endpoint:** `DELETE /api/invitations/{invitationId}`

**Description:** Annule une invitation. Réservé au créateur du scénario.

**Paramètres:**
- `invitationId` (path): ID de l'invitation

**Réponses:**
- `200 OK`: Invitation annulée avec succès
- `401 Unauthorized`: Utilisateur non authentifié
- `403 Forbidden`: Utilisateur non autorisé (pas le créateur du scénario)
- `404 Not Found`: Invitation non trouvée

## Endpoints pour la gestion des sessions de jeu

### Démarrer une partie

**Endpoint:** `POST /api/games/maps/{mapId}/start`

**Description:** Démarre une partie sur une carte. Réservé au propriétaire de la carte.

**Paramètres:**
- `mapId` (path): ID de la carte
- `scenarioId` (query): ID du scénario à démarrer

**Réponses:**
- `200 OK`: Partie démarrée avec succès
- `401 Unauthorized`: Utilisateur non authentifié
- `403 Forbidden`: Utilisateur non autorisé (pas le propriétaire)
- `404 Not Found`: Carte non trouvée
- `400 Bad Request`: Scénario non trouvé ou non associé à cette carte

### Terminer une partie

**Endpoint:** `POST /api/games/maps/{mapId}/end`

**Description:** Termine une partie sur une carte. Réservé au propriétaire de la carte.

**Paramètres:**
- `mapId` (path): ID de la carte

**Réponses:**
- `200 OK`: Partie terminée avec succès
- `401 Unauthorized`: Utilisateur non authentifié
- `403 Forbidden`: Utilisateur non autorisé (pas le propriétaire)
- `404 Not Found`: Carte non trouvée
