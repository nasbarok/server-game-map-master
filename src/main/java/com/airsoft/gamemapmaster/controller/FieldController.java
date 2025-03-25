package com.airsoft.gamemapmaster.controller;

import com.airsoft.gamemapmaster.model.Field;
import com.airsoft.gamemapmaster.model.User;
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
}
