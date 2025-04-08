package com.airsoft.gamemapmaster.controller;

import com.airsoft.gamemapmaster.model.*;
import com.airsoft.gamemapmaster.repository.FieldUserHistoryRepository;
import com.airsoft.gamemapmaster.service.*;
import com.airsoft.gamemapmaster.websocket.WebSocketMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/fields")
public class FieldController {

    private static final Logger logger = LoggerFactory.getLogger(FieldController.class);

    @Autowired
    private FieldService fieldService;

    @Autowired
    private UserService userService;

    @Autowired
    private ConnectedPlayerService connectedPlayerService;

    @Autowired
    private FieldUserHistoryRepository historyRepository;
    @Autowired
    private GameMapService gameMapService;

    @Autowired
    private FieldUserHistoryService fieldUserHistoryService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping
    public ResponseEntity<List<Field>> getAllFields() {
        return ResponseEntity.ok(fieldService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Field> getFieldById(@PathVariable Long id) {
        return fieldService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Field> createField(@RequestBody Field field, @AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> owner = userService.findByUsername(userDetails.getUsername()); // Récupère l'utilisateur depuis ton UserService
        if (owner.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        field.setOpenedAt(LocalDateTime.now());
        field.setActive(true);
        field.setOwner(owner.get()); // Associe le terrain au propriétaire connecté
        Field saved = fieldService.save(field);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Field> updateField(@PathVariable Long id, @RequestBody Field field) {
        return fieldService.findById(id)
                .map(existingField -> {
                    field.setId(id);
                    return ResponseEntity.ok(fieldService.save(field));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteField(@PathVariable Long id) {
        return fieldService.findById(id)
                .map(field -> {
                    fieldService.deleteById(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<Field>> getFieldsByOwnerId(@PathVariable Long ownerId) {
        return ResponseEntity.ok(fieldService.findByOwnerId(ownerId));
    }

    @GetMapping("/owner/self")
    public ResponseEntity<List<Field>> getMyFields(Principal principal) {
        String username = principal.getName();
        Optional<User> user = userService.findByUsername(username);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        logger.info("User found: {}", user.get());
        List<Field> fields = fieldService.findByOwnerId(user.get().getId());
        logger.info("Fields found: {}", fields);
        return ResponseEntity.ok(fields);
    }

    @PostMapping("/{fieldId}/open")
    public ResponseEntity<?> openField(@PathVariable Long fieldId, Principal principal) {
        String username = principal.getName();
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Field> fieldOpt = fieldService.findById(fieldId);
        if (fieldOpt.isEmpty()) return ResponseEntity.notFound().build();

        Field field = fieldOpt.get();
        if (!field.getOwner().getId().equals(userOpt.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Vous n'êtes pas propriétaire de ce terrain.");
        }

        if (field.isActive()) {
            return ResponseEntity.ok(field);
        }

        field.setOpenedAt(LocalDateTime.now());
        field.setClosedAt(null);
        field.setActive(true);

        Field updated = fieldService.save(field);

        // Envoyer une notification WebSocket pour informer tous les clients
        WebSocketMessage openMessage = new WebSocketMessage(
                "FIELD_OPENED",
                Map.of(
                        "fieldId", fieldId,
                        "ownerId", userOpt.get().getId(),
                        "ownerUsername", username,
                        "openedAt", field.getOpenedAt().toString()
                ),
                userOpt.get().getId(),
                System.currentTimeMillis()
        );

        messagingTemplate.convertAndSend("/topic/field/" + fieldId, openMessage);

        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{fieldId}/close")
    public ResponseEntity<?> closeField(@PathVariable Long fieldId, Principal principal) {
        String username = principal.getName();
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Field> fieldOpt = fieldService.findById(fieldId);
        if (fieldOpt.isEmpty()) return ResponseEntity.notFound().build();

        Field field = fieldOpt.get();
        if (!field.getOwner().getId().equals(userOpt.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Vous n'êtes pas propriétaire de ce terrain.");
        }

        if (!field.isActive()) {
            return ResponseEntity.ok(field);
        }

        // Envoyer une notification WebSocket AVANT de déconnecter les joueurs
        WebSocketMessage closeMessage = new WebSocketMessage(
                "FIELD_CLOSED",
                Map.of(
                        "fieldId", fieldId,
                        "ownerId", userOpt.get().getId(),
                        "ownerUsername", username,
                        "closedAt", LocalDateTime.now().toString()
                ),
                userOpt.get().getId(),
                System.currentTimeMillis()
        );

        messagingTemplate.convertAndSend("/topic/field/" + fieldId, closeMessage);

        connectedPlayerService.disconnectAllPlayersFromField(fieldId);

        List<GameMap> gameMap = gameMapService.findByFieldId(fieldId);
        for (GameMap map : gameMap) {
            map.setField(null);
            gameMapService.save(map);
        }

        field.setClosedAt(LocalDateTime.now());
        field.setActive(false);

        Field updated = fieldService.save(field);

        return ResponseEntity.ok(updated);
    }

    @GetMapping("/active/self")
    public ResponseEntity<?> getActiveFieldForCurrentUser(Principal principal) {
        String username = principal.getName();
        Optional<User> userOpt = userService.findByUsername(username);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }

        User user = userOpt.get();

        // 🔍 Recherche du terrain actif appartenant à l'utilisateur
        List<Field> activeFields = fieldService.findByOwnerIdAndActiveTrue(user.getId());

        if (activeFields.isEmpty()) {
            return ResponseEntity.ok().body(null); // Aucun terrain ouvert
        }

        Field field = activeFields.get(0); // On suppose un seul terrain actif max par owner

        return ResponseEntity.ok(field);
    }

    @GetMapping("/active/current")
    public ResponseEntity<?> getActiveFieldForCurrentConnectedPlayer(Principal principal) {
        String username = principal.getName();
        Optional<User> userOpt = userService.findByUsername(username);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }

        User user = userOpt.get();

        // 1️⃣ Si l'utilisateur est propriétaire d'un terrain actif
        Optional<Field> optionnalActiveField = fieldService.findLastOpenedFieldByOwner(user.getId());
        if (!optionnalActiveField.isEmpty()) {
            Field activeField = optionnalActiveField.get(); // un seul terrain actif par owner
            return ResponseEntity.ok(Map.of(
                    "active", true,
                    "role", "HOST",
                    "field", activeField
            ));
        }

        // 2️⃣ Sinon, vérifier s'il est un joueur actuellement dans une session ouverte
        Optional<FieldUserHistory> activeSession =
                historyRepository.findTopByUserIdAndSessionClosedFalseOrderByFieldIdDesc(user.getId());

        if (activeSession.isPresent()) {
            Field activeField = activeSession.get().getField();

            boolean isStillConnected = connectedPlayerService.isPlayerConnectedToField(activeField.getId(), user.getId());

            if (isStillConnected) {
                // ✅ Toujours connecté → OK
                User host = activeField.getOwner();
                host.setPassword(null);
                activeField.setOwner(host);
                return ResponseEntity.ok(Map.of(
                        "active", true,
                        "role", "GAMER",
                        "field", activeField
                ));
            } else {
                // ❌ N'est plus connecté → Pas de terrain actif
                return ResponseEntity.ok(Map.of("active", false));
            }
        }

        // ❌ Aucun terrain actif trouvé pour ce joueur
        return ResponseEntity.ok(Map.of("active", false));
    }




}
