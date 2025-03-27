package com.airsoft.gamemapmaster.controller;

import com.airsoft.gamemapmaster.model.ConnectedPlayer;
import com.airsoft.gamemapmaster.model.Field;
import com.airsoft.gamemapmaster.model.GameMap;
import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.service.ConnectedPlayerService;
import com.airsoft.gamemapmaster.service.FieldService;
import com.airsoft.gamemapmaster.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
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
            return ResponseEntity.badRequest().body("Le terrain est déjà ouvert.");
        }

        field.setOpenedAt(LocalDateTime.now());
        field.setClosedAt(null);
        field.setActive(true);

        Field updated = fieldService.save(field);
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
            return ResponseEntity.badRequest().body("Le terrain est déjà fermé.");
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

        // 🔍 Trouver tous les ConnectedPlayers actifs de cet utilisateur
        List<ConnectedPlayer> connections = connectedPlayerService.findActiveConnectionsByUserId(user.getId());

        if (connections.isEmpty()) {
            return ResponseEntity.ok().body(null); // Pas de connexion active
        }

        // 🔁 Récupérer la première GameMap et le Field associé
        GameMap map = connections.get(0).getGameMap();
        Field field = map.getField();

        return ResponseEntity.ok(field);
    }

}
