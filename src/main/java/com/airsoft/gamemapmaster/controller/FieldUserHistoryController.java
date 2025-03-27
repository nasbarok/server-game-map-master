package com.airsoft.gamemapmaster.controller;

import com.airsoft.gamemapmaster.model.FieldUserHistory;
import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.service.FieldUserHistoryService;
import com.airsoft.gamemapmaster.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/field-history")
public class FieldUserHistoryController {

    @Autowired
    private FieldUserHistoryService historyService;

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

        List<FieldUserHistory> history = historyService.getHistoryForUser(userOpt.get().getId());
        return ResponseEntity.ok(history);
    }

    /**
     * 📋 Liste de tous les joueurs ayant été connectés à un terrain spécifique
     */
    @GetMapping("/field/{fieldId}")
    public ResponseEntity<List<FieldUserHistory>> getHistoryForField(@PathVariable Long fieldId) {
        List<FieldUserHistory> history = historyService.getHistoryForField(fieldId);
        return ResponseEntity.ok(history);
    }

    /**
     * ✅ Appeler cela manuellement pour fermer toutes les sessions actives d'un terrain (par l'hôte)
     */
    @PostMapping("/field/{fieldId}/close")
    public ResponseEntity<?> closeFieldSessions(@PathVariable Long fieldId) {
        historyService.closeSessionsForField(fieldId);
        return ResponseEntity.ok(Map.of("message", "Sessions du terrain fermées"));
    }
}

