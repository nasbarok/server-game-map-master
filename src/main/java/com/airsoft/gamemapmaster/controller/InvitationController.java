package com.airsoft.gamemapmaster.controller;

import com.airsoft.gamemapmaster.model.DTO.CreateInvitationRequest;
import com.airsoft.gamemapmaster.model.DTO.InvitationDTO;
import com.airsoft.gamemapmaster.model.DTO.RespondInvitationRequest;
import com.airsoft.gamemapmaster.model.Field;
import com.airsoft.gamemapmaster.model.Invitation;
import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.security.AuthUser;
import com.airsoft.gamemapmaster.security.jwt.JwtAuthenticationFilter;
import com.airsoft.gamemapmaster.service.FieldService;
import com.airsoft.gamemapmaster.service.InvitationService;
import com.airsoft.gamemapmaster.service.ScenarioService;
import com.airsoft.gamemapmaster.service.UserService;
import com.airsoft.gamemapmaster.websocket.WebSocketMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/invitations")
public class InvitationController {
    private static final Logger logger = LoggerFactory.getLogger(InvitationController.class);

    @Autowired
    private InvitationService invitationService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ScenarioService scenarioService;

    @Autowired
    private FieldService fieldService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    /**
     * Crée une invitation pour un utilisateur à rejoindre un scénario
     */
    @PostMapping
    public ResponseEntity<InvitationDTO> createInvitation(
            @Valid @RequestBody CreateInvitationRequest request,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Non authentifié");
        }

        Long currentUserId = ((AuthUser) authentication.getPrincipal()).getId();

        if (request.getTargetUserId() != null && request.getTargetUserId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible de s’auto‑inviter");
        }

        try {
            InvitationDTO invitationDTO = invitationService.createOrGetInvitation(
                    request.getFieldId(), currentUserId, request.getTargetUserId());

            WebSocketMessage invitationMessage = new WebSocketMessage(
                    "INVITATION_RECEIVED",
                    invitationDTO,
                    currentUserId,
                    System.currentTimeMillis()
            );

            logger.info("📩 Envoi d'une invitation de {} à {}", currentUserId, request.getTargetUserId());

            // Envoi vers le canal du joueur cible
            messagingTemplate.convertAndSend("/topic/user/" + request.getTargetUserId(), invitationMessage);

            return ResponseEntity.ok(invitationDTO);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invitation déjà existante", e);
        }
    }

    /**
     * Récupérer les invitations envoyées pour un terrain
     */
    @GetMapping("/sent")
    public ResponseEntity<List<InvitationDTO>> getSentInvitations(
            @RequestParam Long fieldId,
            @AuthenticationPrincipal AuthUser authUser) {

        logger.info("Récupérer les invitations envoyées pour le terrain {}", fieldId);
        if (authUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Non authentifié");
        }
        Long currentUserId = authUser.getId();

        // Vérification que l'utilisateur est bien propriétaire du terrain
        Field field = fieldService.findById(fieldId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Terrain introuvable"));
        if (!field.getOwner().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé : vous n'êtes pas propriétaire de ce terrain");
        }

        try {
            List<InvitationDTO> invitations =
                    invitationService.getSentInvitations(currentUserId, fieldId);
            return ResponseEntity.ok(invitations);

        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Impossible de récupérer les invitations", e);
        }
    }

    /**
     * Récupérer les invitations reçues
     */
    @GetMapping("/received")
    public ResponseEntity<List<InvitationDTO>> getReceivedInvitations(
            @AuthenticationPrincipal AuthUser authUser) {

        logger.info("Récupérer les invitations reçues");
        if (authUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Non authentifié");
        }
        Long currentUserId = authUser.getId();

        try {
            List<InvitationDTO> invitations =
                    invitationService.getReceivedInvitations(currentUserId);
        logger.info("Récupérer {} invitations reçues", invitations.size());

            return ResponseEntity.ok(invitations);

        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Impossible de récupérer les invitations", e);
        }
    }

    /**
     * Répondre à une invitation
     */
    @PostMapping("/{invitationId}/respond")
    public ResponseEntity<InvitationDTO> respondToInvitation(
            @PathVariable Long invitationId,
            @RequestBody RespondInvitationRequest request,
            @AuthenticationPrincipal AuthUser authUser) {

        if (authUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Non authentifié");
        }
        Long currentUserId = authUser.getId();

        try {
            InvitationDTO invitation = invitationService.respondToInvitation(
                    invitationId,
                    currentUserId,
                    request.isAccepted()
            );

            return ResponseEntity.ok(invitation);

        } catch (EntityNotFoundException e) {
            // Invitation inexistante
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (AccessDeniedException e) {
            // L'utilisateur ne peut pas répondre à cette invitation
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé", e);
        } catch (IllegalStateException e) {
            // Invitation expirée ou déjà traitée
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erreur lors du traitement de l'invitation", e);
        }
    }

    /**
     * Annuler une invitation
     */
    @DeleteMapping("/{invitationId}")
    public ResponseEntity<Void> cancelInvitation(
            @PathVariable Long invitationId,
            @AuthenticationPrincipal AuthUser authUser) {
        logger.info("Annuler l'invitation {}", invitationId);
        if (authUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Non authentifié");
        }
        Long currentUserId = authUser.getId();

        try {
            invitationService.cancelInvitation(invitationId, currentUserId);
            return ResponseEntity.ok().build();

        } catch (EntityNotFoundException e) {
            // invitation inexistante
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);

        } catch (org.springframework.security.access.AccessDeniedException e) {
            // l’utilisateur n’est pas l’émetteur (ou non autorisé à annuler).
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé", e);

        } catch (IllegalStateException e) {
            // déjà acceptée/expirée/annulée
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
        }
    }

    /**
     * Compter les invitations en attente (pour les badges)
     */
    @GetMapping("/count/pending")
    public ResponseEntity<Long> countPendingInvitations(
            @RequestParam Long fieldId,
            @AuthenticationPrincipal AuthUser authUser) {

        if (authUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Non authentifié");
        }
        Long currentUserId = authUser.getId();

        // Vérifie que le host est bien propriétaire du terrain
        var field = fieldService.findById(fieldId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Terrain introuvable"));
        if (!field.getOwner().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé : vous n'êtes pas propriétaire de ce terrain");
        }

        try {
            long count = invitationService.countPendingInvitations(currentUserId, fieldId);
            return ResponseEntity.ok(count);

        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Impossible de compter les invitations", e);
        }
    }
    /**
     * Compter les invitations reçues en attente (pour les badges)
     */
    @GetMapping("/count/received")
    public ResponseEntity<Long> countReceivedPendingInvitations(
            @AuthenticationPrincipal AuthUser authUser) {

        if (authUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Non authentifié");
        }
        Long currentUserId = authUser.getId();

        try {
            long count = invitationService.countReceivedPendingInvitations(currentUserId);
            return ResponseEntity.ok(count);

        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Impossible de compter les invitations reçues", e);
        }
    }

    /**
     * Récupère toutes les invitations pour l'utilisateur connecté
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMyInvitations(Authentication authentication) {
        String username = authentication.getName();
        Optional<User> user = userService.findByUsername(username);
        
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }
        
        List<Invitation> invitations = invitationService.getInvitationsForUser(user.get().getId());
        
        return ResponseEntity.ok(invitations);
    }
    
    /**
     * Récupère toutes les invitations en attente pour l'utilisateur connecté
     */
    @GetMapping("/me/pending")
    public ResponseEntity<?> getMyPendingInvitations(Authentication authentication) {
        String username = authentication.getName();
        Optional<User> user = userService.findByUsername(username);
        
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }
        
        List<Invitation> invitations = invitationService.getPendingInvitationsForUser(user.get().getId());
        
        return ResponseEntity.ok(invitations);
    }
    
    /**
     * Accepte une invitation
     */
    @PostMapping("/{invitationId}/accept")
    public ResponseEntity<?> acceptInvitation(@PathVariable("invitationId") Long invitationId,
                                              Authentication authentication) {
        String username = authentication.getName();
        Optional<User> user = userService.findByUsername(username);

        if (user.isEmpty()) {
            logger.warn("Tentative d'accepter une invitation par un utilisateur non trouvé : {}", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }

        Long userId = user.get().getId();
        logger.info("Utilisateur {} (ID: {}) tente d'accepter l'invitation {}", username, userId, invitationId);

        Optional<Invitation> result = invitationService.acceptInvitation(invitationId, userId);

        if (result.isPresent()) {
            logger.info("Invitation {} acceptée avec succès par l'utilisateur {}", invitationId, username);
            return ResponseEntity.ok(result.get());
        } else {
            logger.warn("Échec de l'acceptation de l'invitation {} par l'utilisateur {} : invitation introuvable ou déjà traitée",
                    invitationId, username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Invitation non trouvée ou déjà traitée");
        }
    }

    /**
     * Refuse une invitation
     */
    @PostMapping("/{invitationId}/decline")
    public ResponseEntity<?> declineInvitation(@PathVariable("invitationId") Long invitationId,
                                               Authentication authentication) {
        String username = authentication.getName();
        Optional<User> user = userService.findByUsername(username);

        if (user.isEmpty()) {
            logger.warn("Tentative de refus d'une invitation par un utilisateur non trouvé : {}", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }

        Long userId = user.get().getId();
        logger.info("Utilisateur {} (ID: {}) tente de refuser l'invitation {}", username, userId, invitationId);

        Optional<Invitation> result = invitationService.declineInvitation(invitationId, userId);

        if (result.isPresent()) {
            logger.info("Invitation {} refusée avec succès par l'utilisateur {}", invitationId, username);
            return ResponseEntity.ok(result.get());
        } else {
            logger.warn("Échec du refus de l'invitation {} par l'utilisateur {} : invitation introuvable ou déjà traitée",
                    invitationId, username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Invitation non trouvée ou déjà traitée");
        }
    }
}
