# Documentation du Projet Airsoft Game Map Master

## Table des matières

1. [Introduction](#introduction)
2. [Architecture globale](#architecture-globale)
3. [Serveur Spring Boot](#serveur-spring-boot)
   - [Installation et configuration](#installation-et-configuration-du-serveur)
   - [Structure du projet](#structure-du-projet-serveur)
   - [API REST](#api-rest)
   - [WebSocket](#websocket)
   - [Authentification JWT](#authentification-jwt)
   - [Système modulaire pour les scénarios](#système-modulaire-pour-les-scénarios)
   - [Extension du serveur](#extension-du-serveur)
4. [Application Flutter](#application-flutter)
   - [Installation et configuration](#installation-et-configuration-de-lapplication)
   - [Structure du projet](#structure-du-projet-flutter)
   - [Interfaces utilisateur](#interfaces-utilisateur)
   - [Communication avec le serveur](#communication-avec-le-serveur)
   - [Scanner QR code](#scanner-qr-code)
   - [Extension de l'application](#extension-de-lapplication)
5. [Déploiement](#déploiement)
6. [Maintenance et évolution](#maintenance-et-évolution)

## Introduction

Airsoft Game Map Master est une application mobile permettant de créer et gérer des scénarios d'airsoft via smartphone. L'application est composée de deux parties principales :

1. **Serveur Spring Boot** : Gère la logique métier, les données et la communication entre les utilisateurs.
2. **Application Flutter** : Interface utilisateur mobile pour les rôles "host" (organisateur) et "gamer" (joueur).

Cette documentation vous guidera à travers l'architecture, l'installation, la configuration et l'extension des deux composants du projet.

## Architecture globale

L'architecture du système est basée sur un modèle client-serveur :

- **Serveur** : API REST et WebSocket pour la communication bidirectionnelle
- **Client** : Application Flutter qui consomme l'API et se connecte au WebSocket

La communication entre le client et le serveur est sécurisée par JWT (JSON Web Token) pour l'authentification.

Le système est conçu de manière modulaire pour faciliter l'ajout de nouveaux types de scénarios.

## Serveur Spring Boot

### Installation et configuration du serveur

#### Prérequis

- Java 17 ou supérieur
- Maven 3.6 ou supérieur
- MySQL 8.0 ou supérieur

#### Installation

1. Clonez le dépôt du projet
2. Configurez la base de données dans `src/main/resources/application.properties`
3. Exécutez la commande suivante pour compiler et démarrer le serveur :

```bash
mvn spring-boot:run
```

Le serveur sera accessible à l'adresse `http://localhost:8080`.

### Structure du projet serveur

Le projet est organisé selon la structure standard de Spring Boot :

```
src/main/java/com/airsoft/gamemapmaster/
├── GameMapMasterApplication.java       # Point d'entrée de l'application
├── config/                             # Configuration Spring
├── controller/                         # Contrôleurs REST
├── model/                              # Entités JPA
├── repository/                         # Repositories Spring Data
├── security/                           # Configuration de sécurité et JWT
├── service/                            # Services métier
├── websocket/                          # Configuration et contrôleurs WebSocket
└── scenario/                           # Système modulaire pour les scénarios
    ├── ScenarioModule.java             # Interface pour les modules de scénario
    ├── ScenarioModuleRegistry.java     # Registre des modules disponibles
    └── treasurehunt/                   # Module de chasse au trésor
```

### API REST

Le serveur expose les endpoints REST suivants :

#### Authentification

- `POST /api/auth/register` : Inscription d'un nouvel utilisateur
- `POST /api/auth/login` : Connexion et obtention d'un token JWT

#### Utilisateurs

- `GET /api/users` : Liste des utilisateurs
- `GET /api/users/{id}` : Détails d'un utilisateur
- `PUT /api/users/{id}` : Mise à jour d'un utilisateur
- `DELETE /api/users/{id}` : Suppression d'un utilisateur

#### Équipes

- `GET /api/teams` : Liste des équipes
- `POST /api/teams` : Création d'une équipe
- `GET /api/teams/{id}` : Détails d'une équipe
- `PUT /api/teams/{id}` : Mise à jour d'une équipe
- `DELETE /api/teams/{id}` : Suppression d'une équipe
- `POST /api/teams/{id}/join` : Rejoindre une équipe

#### Terrains

- `GET /api/fields` : Liste des terrains
- `POST /api/fields` : Création d'un terrain
- `GET /api/fields/{id}` : Détails d'un terrain
- `PUT /api/fields/{id}` : Mise à jour d'un terrain
- `DELETE /api/fields/{id}` : Suppression d'un terrain

#### Cartes

- `GET /api/maps` : Liste des cartes
- `POST /api/maps` : Création d'une carte
- `GET /api/maps/{id}` : Détails d'une carte
- `PUT /api/maps/{id}` : Mise à jour d'une carte
- `DELETE /api/maps/{id}` : Suppression d'une carte

#### Scénarios

- `GET /api/scenarios` : Liste des scénarios
- `POST /api/scenarios` : Création d'un scénario
- `GET /api/scenarios/{id}` : Détails d'un scénario
- `PUT /api/scenarios/{id}` : Mise à jour d'un scénario
- `DELETE /api/scenarios/{id}` : Suppression d'un scénario

#### Invitations

- `POST /api/invitations/generate` : Génération d'un code d'invitation
- `POST /api/games/join` : Rejoindre une partie avec un code d'invitation

### WebSocket

Le serveur utilise STOMP sur WebSocket pour la communication en temps réel. Les endpoints WebSocket sont :

- `/ws` : Point d'entrée WebSocket
- `/topic/game/{gameId}` : Canal de diffusion pour les événements d'une partie
- `/topic/user/{userId}` : Canal de diffusion pour les événements spécifiques à un utilisateur

Les messages WebSocket sont structurés comme suit :

```json
{
  "type": "EVENT_TYPE",
  "payload": {
    // Données spécifiques à l'événement
  }
}
```

Types d'événements supportés :
- `GAME_STARTED` : Une partie a commencé
- `GAME_ENDED` : Une partie s'est terminée
- `PLAYER_JOINED` : Un joueur a rejoint la partie
- `PLAYER_LEFT` : Un joueur a quitté la partie
- `TREASURE_FOUND` : Un trésor a été trouvé (scénario chasse au trésor)
- `INVITATION_RECEIVED` : Invitation à rejoindre une partie

### Authentification JWT

L'authentification utilise JWT (JSON Web Token) pour sécuriser l'API REST et les connexions WebSocket.

Processus d'authentification :
1. L'utilisateur s'inscrit ou se connecte via l'API REST
2. Le serveur génère un token JWT et le renvoie au client
3. Le client inclut ce token dans l'en-tête `Authorization` de chaque requête HTTP
4. Pour WebSocket, le token est passé comme paramètre de requête lors de la connexion

Configuration JWT dans `WebSecurityConfig.java` et `JwtTokenProvider.java`.

### Système modulaire pour les scénarios

Le serveur implémente un système modulaire pour faciliter l'ajout de nouveaux types de scénarios :

1. **Interface ScenarioModule** : Définit le contrat pour tous les modules de scénario
2. **ScenarioModuleRegistry** : Gère l'enregistrement et la récupération des modules
3. **Module TreasureHunt** : Implémentation du scénario de chasse au trésor

Pour ajouter un nouveau type de scénario :
1. Créez un package dans `scenario/` pour votre nouveau scénario
2. Implémentez l'interface `ScenarioModule`
3. Créez les modèles, repositories et services nécessaires
4. Enregistrez votre module dans `ScenarioModuleRegistry`

### Extension du serveur

Pour étendre le serveur avec de nouvelles fonctionnalités :

1. **Nouveaux endpoints REST** : Ajoutez des classes dans le package `controller/`
2. **Nouvelles entités** : Créez des classes dans `model/` et les repositories correspondants
3. **Nouveaux services** : Implémentez la logique métier dans le package `service/`
4. **Nouveaux événements WebSocket** : Ajoutez des types d'événements et leur gestion dans `websocket/`
5. **Nouveaux scénarios** : Suivez les instructions de la section précédente

## Application Flutter

### Installation et configuration de l'application

#### Prérequis

- Flutter 3.0 ou supérieur
- Dart 2.17 ou supérieur
- Android Studio ou VS Code avec extensions Flutter

#### Installation

1. Clonez le dépôt du projet
2. Installez les dépendances :

```bash
flutter pub get
```

3. Configurez l'URL du serveur dans `lib/services/api_service.dart`
4. Lancez l'application :

```bash
flutter run
```

### Structure du projet Flutter

Le projet est organisé selon une architecture par fonctionnalités :

```
lib/
├── main.dart                # Point d'entrée de l'application
├── app.dart                 # Configuration de l'application
├── models/                  # Modèles de données
├── services/                # Services (API, WebSocket, Auth)
├── screens/                 # Écrans de l'application
│   ├── auth/                # Écrans d'authentification
│   ├── common/              # Écrans communs
│   ├── host/                # Écrans pour les organisateurs
│   └── gamer/               # Écrans pour les joueurs
├── widgets/                 # Widgets réutilisables
└── utils/                   # Utilitaires et helpers
```

### Interfaces utilisateur

L'application propose deux interfaces principales :

#### Interface Host (Organisateur)

- Tableau de bord avec 4 onglets : Terrains, Cartes, Scénarios, Équipes
- Création et gestion des terrains de jeu
- Création et gestion des cartes
- Création et gestion des scénarios
- Création et gestion des équipes
- Génération de codes QR pour inviter les joueurs

#### Interface Gamer (Joueur)

- Tableau de bord avec 2 onglets : Parties, Équipes
- Rejoindre des parties via scan de QR code
- Rejoindre des équipes
- Participer aux scénarios (ex: chasse au trésor)

### Communication avec le serveur

L'application communique avec le serveur de deux manières :

#### API REST

Le service `ApiService` gère les requêtes HTTP vers le serveur :

```dart
// Exemple d'utilisation
final apiService = Provider.of<ApiService>(context, listen: false);
final userData = await apiService.post('auth/login', {
  'username': username,
  'password': password,
});
```

#### WebSocket

Le service `WebSocketService` gère la communication en temps réel :

```dart
// Exemple d'utilisation
final webSocketService = Provider.of<WebSocketService>(context, listen: false);
webSocketService.connect();

// Écoute des messages
webSocketService.messageStream.listen((message) {
  // Traitement du message
});
```

Le widget `WebSocketHandler` traite les messages entrants et met à jour l'interface utilisateur en conséquence.

### Scanner QR code

L'application intègre la fonctionnalité de scan et génération de QR code :

#### Génération (Host)

L'écran `QRCodeGeneratorScreen` permet de générer un QR code pour un scénario spécifique :

1. Sélection d'un scénario dans la liste
2. Génération d'un code d'invitation via l'API
3. Affichage du QR code contenant le code d'invitation

#### Scan (Gamer)

L'écran `QRCodeScannerScreen` permet de scanner un QR code pour rejoindre une partie :

1. Accès à la caméra pour scanner un QR code
2. Décodage du code d'invitation
3. Envoi du code au serveur pour rejoindre la partie

### Extension de l'application

Pour étendre l'application avec de nouvelles fonctionnalités :

1. **Nouveaux modèles** : Ajoutez des classes dans le dossier `models/`
2. **Nouveaux écrans** : Créez des widgets dans le dossier approprié sous `screens/`
3. **Nouveaux services** : Implémentez des services dans le dossier `services/`
4. **Nouveaux widgets** : Créez des composants réutilisables dans `widgets/`

Pour ajouter un nouveau type de scénario côté client :
1. Créez les modèles nécessaires
2. Ajoutez les écrans spécifiques au scénario
3. Implémentez la logique de communication avec le serveur

## Déploiement

### Déploiement du serveur

Pour déployer le serveur en production :

1. Compilez l'application :

```bash
mvn clean package
```

2. Exécutez le JAR généré :

```bash
java -jar target/gamemapmaster-0.0.1-SNAPSHOT.jar
```

Vous pouvez également utiliser Docker pour conteneuriser l'application.

### Déploiement de l'application Flutter

Pour générer une version de production de l'application :

#### Android

```bash
flutter build apk --release
```

Le fichier APK sera disponible dans `build/app/outputs/flutter-apk/app-release.apk`.

#### iOS

```bash
flutter build ios --release
```

Utilisez Xcode pour générer l'IPA et le soumettre à l'App Store.

## Maintenance et évolution

### Mises à jour

- **Serveur** : Mettez à jour les dépendances dans `pom.xml`
- **Application** : Mettez à jour les dépendances dans `pubspec.yaml`

### Évolutions futures

Voici quelques idées d'évolutions pour le projet :

1. **Nouveaux types de scénarios** :
   - Capture de drapeau
   - Contrôle de zones
   - Missions objectives

2. **Améliorations de l'interface** :
   - Mode sombre
   - Personnalisation des thèmes
   - Animations avancées

3. **Fonctionnalités avancées** :
   - Intégration de cartes Google Maps
   - Suivi GPS des joueurs
   - Statistiques et classements
   - Chat intégré

4. **Monétisation** :
   - Version host payante avec abonnement
   - Version gamer gratuite avec publicités optionnelles
   - Achats in-app pour des fonctionnalités premium
