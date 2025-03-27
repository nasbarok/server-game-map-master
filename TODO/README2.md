# Modifications pour résoudre les problèmes de gestion des joueurs

Ce dossier contient les fichiers modifiés pour résoudre les problèmes suivants :

1. **Mise à jour de la liste des joueurs** : La liste des joueurs ne se mettait pas à jour correctement dans l'application Flutter côté gamer.
2. **L'hôte n'était pas comptabilisé comme joueur** : L'hôte (qui invite) n'était pas automatiquement comptabilisé comme joueur.
3. **Absence de bouton pour l'hôte** : Il manquait un bouton sur le dashboard permettant à l'hôte de choisir s'il participe ou non en tant que joueur.

## Fichiers modifiés

### 1. `lib/services/game_state_service.dart`

Modifications apportées :
- Ajout d'une référence au service de connexion des joueurs (`PlayerConnectionService`)
- Implémentation d'un système de mise à jour périodique (toutes les 5 secondes) de la liste des joueurs connectés
- Ajout de méthodes pour démarrer/arrêter les mises à jour périodiques
- Modification des méthodes `toggleTerrainOpen()` et `reset()` pour gérer correctement les mises à jour

### 2. `lib/screens/host/terrain_dashboard_screen.dart`

Modifications apportées :
- Ajout d'un switch permettant à l'hôte de se comptabiliser ou non comme joueur
- Implémentation des méthodes `_addHostAsPlayer()` et `_removeHostAsPlayer()` pour gérer l'ajout/retrait de l'hôte
- Modification de l'affichage des joueurs connectés pour utiliser les données réelles de `connectedPlayersList` au lieu des données simulées

## Comment intégrer ces modifications

1. Remplacez les fichiers existants par ceux fournis dans ce dossier
2. Assurez-vous que le `PlayerConnectionService` est correctement injecté dans le `GameStateService` lors de sa création
3. Vérifiez que les imports sont corrects dans tous les fichiers

## Remarques importantes

- Le `GameStateService` nécessite maintenant une instance de `PlayerConnectionService` dans son constructeur
- Assurez-vous que cette dépendance est correctement fournie lors de l'initialisation du service, par exemple :

```dart
// Dans main.dart ou là où vous initialisez vos services
final playerConnectionService = PlayerConnectionService(
  baseUrl: 'https://votre-api.com',
  client: http.Client(),
);

final gameStateService = GameStateService(
  playerConnectionService: playerConnectionService,
);
```

- Si vous utilisez Provider pour l'injection de dépendances, assurez-vous de mettre à jour votre arbre de providers pour refléter cette nouvelle dépendance.
