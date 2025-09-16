package com.airsoft.gamemapmaster.controller;

import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.security.jwt.JwtTokenProvider;
import com.airsoft.gamemapmaster.service.PlayerFavoriteService;
import com.airsoft.gamemapmaster.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(origins = "*")
public class PlayerFavoriteController {

    @Autowired
    private PlayerFavoriteService playerFavoriteService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(PlayerFavoriteController.class);

    /**
     * Ajouter un joueur aux favoris
     */
    @PostMapping("/add")
    public ResponseEntity<?> addToFavorites(
            @RequestBody Map<String, Object> request,
            Principal principal) {

        try {
            String username = principal.getName();
            Optional<User> userOpt = userService.findByUsername(username);

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Long hostId = userOpt.get().getId();
            Long playerId = Long.valueOf(request.get("playerId").toString());

            logger.info("🌟 Requête ajout favori: host={}, player={}", hostId, playerId);

            playerFavoriteService.addToFavorites(hostId, playerId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Joueur ajouté aux favoris");

            return ResponseEntity.ok(response);

        } catch (EntityNotFoundException e) {
            logger.error("❌ Entité non trouvée: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));

        } catch (IllegalArgumentException e) {
            logger.error("❌ Argument invalide: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));

        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'ajout aux favoris", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Erreur interne du serveur"));
        }
    }

    /**
     * Retirer un joueur des favoris
     */
    @DeleteMapping("/remove/{playerId}")
    public ResponseEntity<?> removeFromFavorites(
            @PathVariable Long playerId,
            Principal principal) {

        try {
            String username = principal.getName();
            Optional<User> userOpt = userService.findByUsername(username);

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Long hostId = userOpt.get().getId();

            logger.info("🗑️ Requête suppression favori: host={}, player={}", hostId, playerId);

            playerFavoriteService.removeFromFavorites(hostId, playerId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Joueur retiré des favoris");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Erreur lors de la suppression des favoris", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Erreur interne du serveur"));
        }
    }

    /**
     * Récupérer la liste des favoris (IDs seulement)
     */
    @GetMapping
    public ResponseEntity<?> getFavorites(Principal principal) {
        try {
            String username = principal.getName();
            Optional<User> userOpt = userService.findByUsername(username);

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Long hostId = userOpt.get().getId();

            logger.debug("📋 Requête liste favoris pour host: {}", hostId);

            List<Long> favoriteIds = playerFavoriteService.getFavoritePlayerIds(hostId);

            List<Map<String, Object>> favorites = favoriteIds.stream()
                    .map(id -> Map.of("playerId", (Object) id))
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("favorites", favorites);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des favoris", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Erreur interne du serveur"));
        }
    }

    /**
     * Récupérer les détails des joueurs favoris
     */
    @GetMapping("/details")
    public ResponseEntity<?> getFavoritePlayersDetails(Principal principal) {
        try {
            String username = principal.getName();
            Optional<User> userOpt = userService.findByUsername(username);

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Long hostId = userOpt.get().getId();

            logger.debug("📋 Requête détails favoris pour host: {}", hostId);

            List<User> favoritePlayers = playerFavoriteService.getFavoritePlayersDetails(hostId);

            List<Map<String, Object>> playersData = favoritePlayers.stream()
                    .map(player -> {
                        Map<String, Object> playerMap = new HashMap<>();
                        playerMap.put("id", player.getId());
                        playerMap.put("username", player.getUsername());
                        playerMap.put("email", player.getEmail());
                        return playerMap;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("players", playersData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des détails favoris", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Erreur interne du serveur"));
        }
    }
}