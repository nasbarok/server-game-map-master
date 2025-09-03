package com.airsoft.gamemapmaster.controller;

import com.airsoft.gamemapmaster.model.DTO.FieldDTO;
import com.airsoft.gamemapmaster.model.Field;
import com.airsoft.gamemapmaster.model.FieldUserHistory;
import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.service.FieldUserHistoryService;
import com.airsoft.gamemapmaster.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

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
    public ResponseEntity<?> getFieldHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Authentication authentication) {

        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }

        User user = userOpt.get();

        // Pagination et tri par openedAt (plus récent en premier)
        int safeSize = Math.min(Math.max(size, 1), 100); // limiter la taille
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(Sort.Order.desc("openedAt").nullsLast(), Sort.Order.desc("id")));

        // Utiliser un repository qui retourne un Page (paginé)
        Page<Field> fieldsPage = fieldUserHistoryService.getFieldsVisitedByUser(user.getId(), pageable);

        // Mapping vers FieldDTO pour ne pas exposer l’entité directement
        Page<FieldDTO> dtoPage = fieldsPage.map(FieldDTO::fromEntity);

        // Création de la réponse paginée standard
        Map<String, Object> response = new HashMap<>();
        response.put("content", dtoPage.getContent());
        response.put("totalElements", dtoPage.getTotalElements());
        response.put("totalPages", dtoPage.getTotalPages());
        response.put("number", dtoPage.getNumber());
        response.put("size", dtoPage.getSize());
        response.put("first", dtoPage.isFirst());
        response.put("last", dtoPage.isLast());
        response.put("numberOfElements", dtoPage.getNumberOfElements());

        return ResponseEntity.ok(response);
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

    // Ajout de l'endpoint pour obtenir le dernier terrain actif
    @GetMapping("/last-active")
    public ResponseEntity<?> getLastActiveFieldForUser(Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }

        User user = userOpt.get();

        // Récupérer la liste des terrains actifs pour l'utilisateur
        List<Field> activeFields = fieldUserHistoryService.getLastActivesFieldsForUser(user.getId());

        // Liste pour stocker les DTOs convertis
        List<FieldDTO> activeFieldsDTO = new ArrayList<>();
        // Conversion manuelle des entités Field en FieldDTO
        for (Field field : activeFields) {
            FieldDTO fieldDTO = FieldDTO.fromEntity(field);
            activeFieldsDTO.add(fieldDTO);
        }

        // Retourner la réponse avec la liste de FieldDTO
        return ResponseEntity.ok(activeFieldsDTO);
    }



}

