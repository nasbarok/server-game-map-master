package com.airsoft.gamemapmaster.controller;

import com.airsoft.gamemapmaster.model.Invitation;
import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.security.jwt.JwtAuthenticationFilter;
import com.airsoft.gamemapmaster.service.InvitationService;
import com.airsoft.gamemapmaster.service.ScenarioService;
import com.airsoft.gamemapmaster.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
    
    /**
     * Crée une invitation pour un utilisateur à rejoindre un scénario
     */
    @PostMapping
    public ResponseEntity<?> createInvitation(@RequestParam("scenarioId") Long scenarioId,
                                             @RequestParam("userId") Long userId,
                                             @RequestParam(value = "teamId", required = false) Long teamId,
                                             Authentication authentication) {
        String username = authentication.getName();
        Optional<User> currentUser = userService.findByUsername(username);
        
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }
        
        // Vérifier si l'utilisateur est le créateur du scénario
        return scenarioService.findById(scenarioId)
                .map(scenario -> {
                    if (!scenario.getGameMap().getCreator().getId().equals(currentUser.get().getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body("Vous n'êtes pas autorisé à créer des invitations pour ce scénario");
                    }
                    
                    Invitation invitation = invitationService.createInvitation(scenarioId, userId, teamId);
                    
                    if (invitation == null) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body("Impossible de créer l'invitation");
                    }
                    
                    return ResponseEntity.status(HttpStatus.CREATED).body(invitation);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Scénario non trouvé"));
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
     * Récupère toutes les invitations pour un scénario
     */
    @GetMapping("/scenario/{scenarioId}")
    public ResponseEntity<?> getInvitationsForScenario(@PathVariable("scenarioId") Long scenarioId,
                                                     Authentication authentication) {
        String username = authentication.getName();
        Optional<User> user = userService.findByUsername(username);
        
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }
        
        // Vérifier si l'utilisateur est le créateur du scénario
        return scenarioService.findById(scenarioId)
                .map(scenario -> {
                    if (!scenario.getGameMap().getCreator().getId().equals(user.get().getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body("Vous n'êtes pas autorisé à voir les invitations pour ce scénario");
                    }
                    
                    List<Invitation> invitations = invitationService.getInvitationsForScenario(scenarioId);
                    
                    return ResponseEntity.ok(invitations);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Scénario non trouvé"));
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
    
    /**
     * Annule une invitation
     */
    @DeleteMapping("/{invitationId}")
    public ResponseEntity<?> cancelInvitation(@PathVariable("invitationId") Long invitationId,
                                             Authentication authentication) {
        String username = authentication.getName();
        Optional<User> user = userService.findByUsername(username);
        
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }
        
        boolean cancelled = invitationService.cancelInvitation(invitationId, user.get().getId());
        
        if (!cancelled) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Vous n'êtes pas autorisé à annuler cette invitation ou elle n'existe pas");
        }
        
        return ResponseEntity.ok(Map.of("message", "Invitation annulée avec succès"));
    }
}
