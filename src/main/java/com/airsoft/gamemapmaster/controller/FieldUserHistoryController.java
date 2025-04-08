package com.airsoft.gamemapmaster.controller;

import com.airsoft.gamemapmaster.model.Field;
import com.airsoft.gamemapmaster.model.FieldUserHistory;
import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.service.FieldUserHistoryService;
import com.airsoft.gamemapmaster.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/fields-history")
public class FieldUserHistoryController {

    @Autowired
    private FieldUserHistoryService fieldUserHistoryService;

    @Autowired
    private UserService userService;

    /**
     * 📋 Liste des sessions passées pour le joueur connecté
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMyHistory(Principal principal) {
        Optional<User> userOpt = userService.findByUsername(principal.getName());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }

        List<FieldUserHistory> history = fieldUserHistoryService.getHistoryForUser(userOpt.get().getId());
        return ResponseEntity.ok(history);
    }

    /**
     * 📋 Liste de tous les joueurs ayant été connectés à un terrain spécifique
     */
    @GetMapping("/field/{fieldId}")
    public ResponseEntity<List<FieldUserHistory>> getHistoryForField(@PathVariable Long fieldId) {
        List<FieldUserHistory> history = fieldUserHistoryService.getHistoryForField(fieldId);
        return ResponseEntity.ok(history);
    }

    /**
     * ✅ Appeler cela manuellement pour fermer toutes les sessions actives d'un terrain (par l'hôte)
     */
    @PostMapping("/field/{fieldId}/close")
    public ResponseEntity<?> closeFieldSessions(@PathVariable Long fieldId) {
        fieldUserHistoryService.closeSessionsForField(fieldId);
        return ResponseEntity.ok(Map.of("message", "Sessions du terrain fermées"));
    }

    @GetMapping("/history")
    public ResponseEntity<?> getFieldHistory(Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }

        User user = userOpt.get();

        // Récupérer l'historique des terrains visités par l'utilisateur
        List<Field> fields = fieldUserHistoryService.getFieldsVisitedByUser(user.getId());

        return ResponseEntity.ok(fields);
    }

    @DeleteMapping("/history/{historyId}")
    public ResponseEntity<?> deleteFieldHistoryEntry(@PathVariable Long historyId, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }

        boolean deleted = fieldUserHistoryService.deleteHistoryEntryIfOwnedByUser(historyId, userOpt.get());
        if (deleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accès interdit ou entrée non trouvée");
        }
    }

}

