package com.airsoft.gamemapmaster.scenario.bomboperation.controller;

import com.airsoft.gamemapmaster.model.Scenario;
import com.airsoft.gamemapmaster.scenario.bomboperation.dto.BombOperationScenarioDto;
import com.airsoft.gamemapmaster.scenario.bomboperation.dto.BombSiteDto;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombOperationScenario;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombSite;
import com.airsoft.gamemapmaster.scenario.bomboperation.service.BombOperationScenarioService;
import com.airsoft.gamemapmaster.service.ScenarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/scenarios/bomb-operation")
public class BombOperationScenarioController {

    private static final Logger logger = LoggerFactory.getLogger(BombOperationScenarioController.class);

    @Autowired
    private BombOperationScenarioService bombOperationScenarioService;

    @Autowired
    private ScenarioService scenarioService;

    @PostMapping
    public ResponseEntity<BombOperationScenarioDto> createBombOperationScenario(
            @RequestParam Long scenarioId,
            @RequestParam(required = false) Integer bombTimer,
            @RequestParam(required = false) Integer defuseTime,
            @RequestParam(required = false) Integer activeSites,
            @RequestParam(required = false) String attackTeamName,
            @RequestParam(required = false) String defenseTeamName,
            @RequestParam(required = false) Boolean showZones,
            @RequestParam(required = false) Boolean showPointsOfInterest) {

        logger.info("Création d'un nouveau scénario d'Opération Bombe pour le scénario ID: {}", scenarioId);

        Optional<Scenario> scenarioOpt = scenarioService.findById(scenarioId);

        if (!scenarioOpt.isPresent()) {
            logger.error("Scénario non trouvé pour l'ID: {}", scenarioId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Scenario scenario = scenarioOpt.get();

        BombOperationScenario bombOperationScenario = bombOperationScenarioService.createBombOperationScenario(
                scenario, bombTimer, defuseTime, activeSites, attackTeamName, defenseTeamName, showZones, showPointsOfInterest);

        return new ResponseEntity<>(convertToDto(bombOperationScenario), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BombOperationScenarioDto> updateBombOperationScenario(
            @PathVariable Long id,
            @RequestParam(required = false) Integer bombTimer,
            @RequestParam(required = false) Integer defuseTime,
            @RequestParam(required = false) Integer activeSites,
            @RequestParam(required = false) String attackTeamName,
            @RequestParam(required = false) String defenseTeamName,
            @RequestParam(required = false) Boolean showZones,
            @RequestParam(required = false) Boolean showPointsOfInterest) {

        logger.info("Mise à jour du scénario d'Opération Bombe ID: {}", id);

        BombOperationScenario bombOperationScenario = bombOperationScenarioService.updateBombOperationScenario(
                id, bombTimer, defuseTime, activeSites, attackTeamName, defenseTeamName, showZones, showPointsOfInterest);

        return new ResponseEntity<>(convertToDto(bombOperationScenario), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BombOperationScenarioDto> getBombOperationScenarioById(@PathVariable Long id) {
        logger.info("Récupération du scénario d'Opération Bombe ID: {}", id);

        BombOperationScenario bombOperationScenario = bombOperationScenarioService.getBombOperationScenarioById(id);

        return new ResponseEntity<>(convertToDto(bombOperationScenario), HttpStatus.OK);
    }

    @GetMapping("/by-scenario/{scenarioId}")
    public ResponseEntity<BombOperationScenarioDto> getBombOperationScenarioByScenarioId(@PathVariable Long scenarioId) {
        logger.info("Récupération du scénario d'Opération Bombe par scénario ID: {}", scenarioId);

        BombOperationScenario bombOperationScenario = bombOperationScenarioService.getBombOperationScenarioByScenarioId(scenarioId);

        return new ResponseEntity<>(convertToDto(bombOperationScenario), HttpStatus.OK);
    }

    @GetMapping("/active/{scenarioId}")
    public ResponseEntity<BombOperationScenarioDto> getActiveBombOperationScenario(@PathVariable Long scenarioId) {
        logger.info("Récupération du scénario d'Opération Bombe actif pour le scénario ID: {}", scenarioId);

        BombOperationScenario bombOperationScenario = bombOperationScenarioService.getActiveBombOperationScenario(scenarioId);

        if (bombOperationScenario == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(convertToDto(bombOperationScenario), HttpStatus.OK);
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<BombOperationScenarioDto> activateBombOperationScenario(@PathVariable Long id) {
        logger.info("Activation du scénario d'Opération Bombe ID: {}", id);

        BombOperationScenario bombOperationScenario = bombOperationScenarioService.activateBombOperationScenario(id);

        return new ResponseEntity<>(convertToDto(bombOperationScenario), HttpStatus.OK);
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<BombOperationScenarioDto> deactivateBombOperationScenario(@PathVariable Long id) {
        logger.info("Désactivation du scénario d'Opération Bombe ID: {}", id);

        BombOperationScenario bombOperationScenario = bombOperationScenarioService.deactivateBombOperationScenario(id);

        return new ResponseEntity<>(convertToDto(bombOperationScenario), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBombOperationScenario(@PathVariable Long id) {
        logger.info("Suppression du scénario d'Opération Bombe ID: {}", id);

        bombOperationScenarioService.deleteBombOperationScenario(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{scenarioId}/bomb-sites")
    public ResponseEntity<BombSiteDto> addBombSite(
            @PathVariable Long scenarioId,
            @RequestParam String name,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) Double radius) {

        logger.info("Ajout d'un site de bombe au scénario d'Opération Bombe ID: {}", scenarioId);

        BombSite bombSite = bombOperationScenarioService.addBombSite(scenarioId, name, latitude, longitude, radius);

        return new ResponseEntity<>(convertToDto(bombSite), HttpStatus.CREATED);
    }

    @PutMapping("/bomb-sites/{siteId}")
    public ResponseEntity<BombSiteDto> updateBombSite(
            @PathVariable Long siteId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double radius) {

        logger.info("Mise à jour du site de bombe ID: {}", siteId);

        BombSite bombSite = bombOperationScenarioService.updateBombSite(siteId, name, latitude, longitude, radius);

        return new ResponseEntity<>(convertToDto(bombSite), HttpStatus.OK);
    }

    @GetMapping("/bomb-sites/{siteId}")
    public ResponseEntity<BombSiteDto> getBombSiteById(@PathVariable Long siteId) {
        logger.info("Récupération du site de bombe ID: {}", siteId);

        BombSite bombSite = bombOperationScenarioService.getBombSiteById(siteId);

        return new ResponseEntity<>(convertToDto(bombSite), HttpStatus.OK);
    }

    @GetMapping("/{scenarioId}/bomb-sites")
    public ResponseEntity<List<BombSiteDto>> getBombSitesByScenarioId(@PathVariable Long scenarioId) {
        logger.info("Récupération des sites de bombe pour le scénario d'Opération Bombe ID: {}", scenarioId);

        List<BombSite> bombSites = bombOperationScenarioService.getBombSitesByScenarioId(scenarioId);

        List<BombSiteDto> bombSiteDtos = bombSites.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new ResponseEntity<>(bombSiteDtos, HttpStatus.OK);
    }

    @DeleteMapping("/bomb-sites/{siteId}")
    public ResponseEntity<Void> deleteBombSite(@PathVariable Long siteId) {
        logger.info("Suppression du site de bombe ID: {}", siteId);

        bombOperationScenarioService.deleteBombSite(siteId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private BombOperationScenarioDto convertToDto(BombOperationScenario bombOperationScenario) {
        BombOperationScenarioDto dto = new BombOperationScenarioDto();
        dto.setId(bombOperationScenario.getId());
        dto.setScenarioId(bombOperationScenario.getScenario().getId());
        dto.setBombTimer(bombOperationScenario.getBombTimer());
        dto.setDefuseTime(bombOperationScenario.getDefuseTime());
        dto.setActiveSites(bombOperationScenario.getActiveSites());
        dto.setAttackTeamName(bombOperationScenario.getAttackTeamName());
        dto.setDefenseTeamName(bombOperationScenario.getDefenseTeamName());
        dto.setActive(bombOperationScenario.getActive());
        dto.setShowZones(bombOperationScenario.getShowZones());
        dto.setShowPointsOfInterest(bombOperationScenario.getShowPointsOfInterest());
        return dto;
    }

    private BombSiteDto convertToDto(BombSite bombSite) {
        BombSiteDto dto = new BombSiteDto();
        dto.setId(bombSite.getId());
        dto.setName(bombSite.getName());
        dto.setLatitude(bombSite.getLatitude());
        dto.setLongitude(bombSite.getLongitude());
        dto.setRadius(bombSite.getRadius());
        dto.setBombOperationScenarioId(bombSite.getBombOperationScenario().getId());
        return dto;
    }
}
