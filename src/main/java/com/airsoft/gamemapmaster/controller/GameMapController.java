package com.airsoft.gamemapmaster.controller;

import com.airsoft.gamemapmaster.model.Field;
import com.airsoft.gamemapmaster.model.GameMap;
import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.service.FieldService;
import com.airsoft.gamemapmaster.service.GameMapService;
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
@RequestMapping("/api/maps")
public class GameMapController {

    @Autowired
    private GameMapService gameMapService;

    @Autowired
    private UserService userService;

    @Autowired
    private FieldService fieldService;
    private static final Logger logger = LoggerFactory.getLogger(GameMapController.class);

    @GetMapping
    public ResponseEntity<List<GameMap>> getAllMaps() {
        return ResponseEntity.ok(gameMapService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameMap> getMapById(@PathVariable Long id) {
        return gameMapService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<GameMap> createMap(@RequestBody GameMap gameMap, @AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> owner = userService.findByUsername(userDetails.getUsername()); // Récupère l'utilisateur depuis ton UserService
        if (owner.isEmpty()) {
            logger.warn(" [GameMapController] Tentative de création de carte sans utilisateur authentifié.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        gameMap.setOwner(owner.get()); // Associe le terrain au propriétaire connecté
        gameMap.setCreator(owner.get());

        logger.info("Création de carte demandée par : {}, avec adresse source : {}", owner.get().getUsername(), gameMap.getSourceAddress());

        GameMap saved = gameMapService.save(gameMap);
        logger.info("Carte créée avec succès (ID: {}) avec adresse source sauvegardée : {}", saved.getId(), saved.getSourceAddress());

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GameMap> updateMap(@PathVariable Long id, @RequestBody GameMap gameMapInput) {
        logger.info("🟡 [GameMapController] Reçu PUT /api/maps/{}", id);
        Optional<GameMap> existingOpt = gameMapService.findById(id);
        if (existingOpt.isEmpty()) {
            logger.warn("Carte introuvable avec ID : {}", id);
            return ResponseEntity.notFound().build();
        }

        GameMap existingMap = existingOpt.get();

        logger.info("Mise à jour de la carte (ID: {}). Ancienne adresse : {}, Nouvelle adresse : {}",
                id, existingMap.getSourceAddress(), gameMapInput.getSourceAddress());

        // 🔄 Mise à jour des champs de base
        existingMap.setName(gameMapInput.getName());
        existingMap.setDescription(gameMapInput.getDescription());
        existingMap.setOwner(gameMapInput.getOwner());
        existingMap.setSourceAddress(gameMapInput.getSourceAddress());
        existingMap.setCenterLatitude(gameMapInput.getCenterLatitude());
        existingMap.setCenterLongitude(gameMapInput.getCenterLongitude());
        existingMap.setInitialZoom(gameMapInput.getInitialZoom());
        existingMap.setFieldBoundaryJson(gameMapInput.getFieldBoundaryJson());
        existingMap.setMapZonesJson(gameMapInput.getMapZonesJson());
        existingMap.setMapPointsOfInterestJson(gameMapInput.getMapPointsOfInterestJson());
        existingMap.setBackgroundImageBase64(gameMapInput.getBackgroundImageBase64());
        existingMap.setBackgroundBoundsJson(gameMapInput.getBackgroundBoundsJson());
        existingMap.setSatelliteImageBase64(gameMapInput.getSatelliteImageBase64());
        existingMap.setSatelliteBoundsJson(gameMapInput.getSatelliteBoundsJson());

        // ✅ Mise à jour sécurisée des scénarios
        if (existingMap.getScenarios() != null) {
            existingMap.getScenarios().clear();
            if (gameMapInput.getScenarios() != null) {
                existingMap.getScenarios().addAll(gameMapInput.getScenarios());
            }
        }
        // 🧭 Mise à jour du terrain lié si un fieldId est fourni
        if (gameMapInput.getField() != null) {
            Optional<Field> fieldOpt = fieldService.findById(gameMapInput.getField().getId());
            if (fieldOpt.isPresent()) {
                existingMap.setField(fieldOpt.get());
            } else {
                return ResponseEntity.badRequest().body(null); // ou tu peux lancer une exception personnalisée
            }
        } else {
            existingMap.setField(null); // facultatif : réinitialiser si aucun fieldId fourni
        }

        GameMap saved = gameMapService.save(existingMap);

        logger.info("Carte mise à jour avec succès (ID: {}) avec adresse source : {}", saved.getId(), saved.getSourceAddress());

        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMap(@PathVariable Long id) {
        return gameMapService.findById(id)
                .map(gameMap -> {
                    gameMapService.deleteById(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/field/{fieldId}")
    public ResponseEntity<List<GameMap>> getMapsByFieldId(@PathVariable Long fieldId) {
        return ResponseEntity.ok(gameMapService.findByFieldId(fieldId));
    }
    
    @GetMapping("/creator/{creatorId}")
    public ResponseEntity<List<GameMap>> getMapsByCreatorId(@PathVariable Long creatorId) {
        return ResponseEntity.ok(gameMapService.findByCreatorId(creatorId));
    }

    @GetMapping("/owner/self")
    public ResponseEntity<List<GameMap>> getMyMaps(Principal principal) {
        String username = principal.getName();
        Optional<User> user = userService.findByUsername(username);

        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<GameMap> maps = gameMapService.findByOwnerId(user.get().getId());
        logger.info("Found " + maps.size() + " maps for user " + user.get().getUsername());
        return ResponseEntity.ok(maps);
    }

    @GetMapping(params = "fieldId")
    public ResponseEntity<?> getMapByFieldId(@RequestParam("fieldId") Long fieldId) {
        Optional<GameMap> optionalMap = gameMapService.findFirstByFieldId(fieldId);
        if (optionalMap.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("no_map_found");
        }
        return ResponseEntity.ok(optionalMap.get());
    }

}
