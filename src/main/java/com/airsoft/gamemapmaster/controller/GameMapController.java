package com.airsoft.gamemapmaster.controller;

import com.airsoft.gamemapmaster.model.Field;
import com.airsoft.gamemapmaster.model.GameMap;
import com.airsoft.gamemapmaster.model.User;
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        gameMap.setOwner(owner.get()); // Associe le terrain au propriétaire connecté
        gameMap.setCreator(owner.get());
        GameMap saved = gameMapService.save(gameMap);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GameMap> updateMap(@PathVariable Long id, @RequestBody GameMap gameMap) {
        return gameMapService.findById(id)
                .map(existingMap -> {
                    gameMap.setId(id);
                    return ResponseEntity.ok(gameMapService.save(gameMap));
                })
                .orElse(ResponseEntity.notFound().build());
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

}
