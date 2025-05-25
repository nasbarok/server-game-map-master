package com.airsoft.gamemapmaster.scenario.bomboperation.service.impl;
import com.airsoft.gamemapmaster.model.Scenario;
import com.airsoft.gamemapmaster.scenario.bomboperation.dto.BombSiteDto;
import com.airsoft.gamemapmaster.scenario.bomboperation.exception.BombOperationException;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombOperationScenario;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombSite;
import com.airsoft.gamemapmaster.scenario.bomboperation.repository.BombOperationScenarioRepository;
import com.airsoft.gamemapmaster.scenario.bomboperation.repository.BombSiteRepository;
import com.airsoft.gamemapmaster.scenario.bomboperation.service.BombOperationScenarioService;
import com.airsoft.gamemapmaster.service.ScenarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class BombOperationScenarioServiceImpl implements BombOperationScenarioService {

    private static final Logger logger = LoggerFactory.getLogger(BombOperationScenarioServiceImpl.class);

    @Autowired
    private BombOperationScenarioRepository bombOperationScenarioRepository;

    @Autowired
    private BombSiteRepository bombSiteRepository;

    @Autowired
    private ScenarioService scenarioService;

    @Override
    @Transactional
    public BombOperationScenario createBombOperationScenario(
            Scenario scenario,
            Integer bombTimer,
            Integer defuseTime,
            Integer activeSites,
            String attackTeamName,
            String defenseTeamName,
            Boolean showZones,
            Boolean showPointsOfInterest
    ) {
        logger.info("Création d'un nouveau scénario d'Opération Bombe pour le scénario ID: {}", scenario.getId());

        BombOperationScenario bombOperationScenario = new BombOperationScenario();
        bombOperationScenario.setScenario(scenario);

        if (bombTimer != null) {
            bombOperationScenario.setBombTimer(bombTimer);
        }

        if (defuseTime != null) {
            bombOperationScenario.setDefuseTime(defuseTime);
        }

        if (activeSites != null) {
            bombOperationScenario.setActiveSites(activeSites);
        }

        if (attackTeamName != null && !attackTeamName.isEmpty()) {
            bombOperationScenario.setAttackTeamName(attackTeamName);
        }

        if (defenseTeamName != null && !defenseTeamName.isEmpty()) {
            bombOperationScenario.setDefenseTeamName(defenseTeamName);
        }

        if (showZones != null) {
            bombOperationScenario.setShowZones(showZones);
        }

        if (showPointsOfInterest != null) {
            bombOperationScenario.setShowPointsOfInterest(showPointsOfInterest);
        }

        bombOperationScenario = bombOperationScenarioRepository.save(bombOperationScenario);
        logger.info("Scénario d'Opération Bombe créé avec l'ID: {}", bombOperationScenario.getId());

        return bombOperationScenario;
    }

    @Override
    @Transactional
    public BombOperationScenario updateBombOperationScenario(
            Long id,
            Integer bombTimer,
            Integer defuseTime,
            Integer activeSites,
            String attackTeamName,
            String defenseTeamName,
            Boolean showZones,
            Boolean showPointsOfInterest
    ) {
        logger.info("Mise à jour du scénario d'Opération Bombe ID: {}", id);

        BombOperationScenario bombOperationScenario = getBombOperationScenarioById(id);

        if (bombTimer != null) {
            bombOperationScenario.setBombTimer(bombTimer);
        }

        if (defuseTime != null) {
            bombOperationScenario.setDefuseTime(defuseTime);
        }

        if (activeSites != null) {
            bombOperationScenario.setActiveSites(activeSites);
        }

        if (attackTeamName != null && !attackTeamName.isEmpty()) {
            bombOperationScenario.setAttackTeamName(attackTeamName);
        }

        if (defenseTeamName != null && !defenseTeamName.isEmpty()) {
            bombOperationScenario.setDefenseTeamName(defenseTeamName);
        }

        if (showZones != null) {
            bombOperationScenario.setShowZones(showZones);
        }

        if (showPointsOfInterest != null) {
            bombOperationScenario.setShowPointsOfInterest(showPointsOfInterest);
        }

        bombOperationScenario = bombOperationScenarioRepository.save(bombOperationScenario);
        logger.info("Scénario d'Opération Bombe mis à jour: {}", bombOperationScenario.getId());

        return bombOperationScenario;
    }

    @Override
    public BombOperationScenario getBombOperationScenarioById(Long id) {
        logger.info("Récupération du scénario d'Opération Bombe ID: {}", id);
        return bombOperationScenarioRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Scénario d'Opération Bombe non trouvé avec l'ID: {}", id);
                    return new BombOperationException.ScenarioNotFoundException(id);
                });
    }

    @Override
    public BombOperationScenario getBombOperationScenarioByScenarioId(Long scenarioId) {
        logger.info("Récupération du scénario d'Opération Bombe par scénario ID: {}", scenarioId);
        Optional<BombOperationScenario> scenario = bombOperationScenarioRepository.findByScenarioId(scenarioId);

        if (scenario.isEmpty()) {
            logger.error("Aucun scénario d'Opération Bombe trouvé pour le scénario ID: {}", scenarioId);
            throw new BombOperationException.ScenarioNotFoundException(scenarioId);
        }

        return scenario.get();
    }

    @Override
    public BombOperationScenario getActiveBombOperationScenario(Long scenarioId) {
        logger.info("Récupération du scénario d'Opération Bombe actif pour le scénario ID: {}", scenarioId);
        return bombOperationScenarioRepository.findByScenarioIdAndActiveTrue(scenarioId)
                .orElse(null);
    }


    @Override
    @Transactional
    public void deleteBombOperationScenario(Long id) {
        logger.info("Suppression du scénario d'Opération Bombe ID: {}", id);

        // Récupérer le scénario d'Opération Bombe
        BombOperationScenario bombOperationScenario = getBombOperationScenarioById(id);

        // Supprimer les BombSites associés
        deleteBombSitesByScenarioId(bombOperationScenario);

        // Supprimer le scénario d'Opération Bombe
        bombOperationScenarioRepository.delete(bombOperationScenario);
        logger.info("Scénario d'Opération Bombe supprimé: {}", id);
    }

    private void deleteBombSitesByScenarioId(BombOperationScenario bombOperationScenario) {
        // Supprimer tous les BombSites associés au BombOperationScenario
        Set<BombSite> bombSites = bombOperationScenario.getBombSites();
        if (bombSites != null) {
            for (BombSite bombSite : bombSites) {
                // Supprimer chaque BombSite
                bombSiteRepository.delete(bombSite);
                logger.info("BombSite supprimé: {}", bombSite.getId());
            }
        }
    }
    @Override
    @Transactional
    public BombSite addBombSite(Long scenarioId,Long bombOperationScenarioId, String name, Double latitude, Double longitude, Double radius) {
        logger.info("[BombOperationScenarioServiceImpl] Ajout d'un site de bombe au scénario d'Opération Bombe ID: {}", scenarioId);

        BombOperationScenario bombOperationScenario = getBombOperationScenarioById(bombOperationScenarioId);

        BombSite bombSite = new BombSite();
        bombSite.setName(name);
        bombSite.setLatitude(latitude);
        bombSite.setLongitude(longitude);

        if (radius != null) {
            bombSite.setRadius(radius);
        }
        bombSite.setBombOperationScenario(bombOperationScenario);

        bombSite = bombSiteRepository.save(bombSite);
        bombSite.setScenarioId(scenarioId); // Set scenarioId for easier queries

        logger.info("Site de bombe ajouté avec l'ID: {}", bombSite.getId());


        return bombSite;
    }

    private BombSiteDto convertToDto(BombSite bombSite) {
        BombSiteDto dto = new BombSiteDto();
        dto.setId(bombSite.getId());
        dto.setName(bombSite.getName());
        dto.setLatitude(bombSite.getLatitude());
        dto.setLongitude(bombSite.getLongitude());
        dto.setRadius(bombSite.getRadius());
        dto.setBombOperationScenarioId(bombSite.getBombOperationScenario().getId());
        dto.setScenarioId(bombSite.getBombOperationScenario().getScenario() != null ? bombSite.getBombOperationScenario().getScenario().getId() : null);
        return dto;
    }

    @Override
    @Transactional
    public BombSite updateBombSite(Long siteId, String name, Double latitude, Double longitude, Double radius) {
        logger.info("Mise à jour du site de bombe ID: {}", siteId);

        BombSite bombSite = getBombSiteById(siteId);

        if (name != null && !name.isEmpty()) {
            bombSite.setName(name);
        }

        if (latitude != null) {
            bombSite.setLatitude(latitude);
        }

        if (longitude != null) {
            bombSite.setLongitude(longitude);
        }

        if (radius != null) {
            bombSite.setRadius(radius);
        }

        bombSite = bombSiteRepository.save(bombSite);
        logger.info("Site de bombe mis à jour: {}", bombSite.getId());

        return bombSite;
    }

    @Override
    public BombSite getBombSiteById(Long siteId) {
        logger.info("Récupération du site de bombe ID: {}", siteId);
        return bombSiteRepository.findById(siteId)
                .orElseThrow(() -> {
                    logger.error("Site de bombe non trouvé avec l'ID: {}", siteId);
                    return new BombOperationException.BombSiteNotFoundException(siteId);
                });
    }

    @Override
    public List<BombSite> getBombSitesByScenarioId(Long bombOperationScenarioId) {
        logger.info("Récupération des sites de bombe pour le scénario d'Opération Bombe ID: {}", bombOperationScenarioId);
        return bombSiteRepository.findByBombOperationScenarioId(bombOperationScenarioId);
    }

    @Override
    @Transactional
    public void deleteBombSite(Long siteId) {
        logger.info("Suppression du site de bombe ID: {}", siteId);

        BombSite bombSite = getBombSiteById(siteId);
        bombSiteRepository.delete(bombSite);
        logger.info("Site de bombe supprimé: {}", siteId);
    }

    @Override
    public Optional<BombOperationScenario> findByScenarioId(Long scenarioId) {
        try {
            // Recherche du scénario Opération Bombe associé à l'ID du scénario
            return bombOperationScenarioRepository.findByScenarioId(scenarioId);
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche du BombOperationScenario pour l'ID: {}", scenarioId, e);
            return Optional.empty();
        }
    }

    @Override
    public BombOperationScenario saveBombOperationScenario(BombOperationScenario newScenario) {
        try {
            // Enregistrement du scénario dans la base de données
            return bombOperationScenarioRepository.save(newScenario);
        } catch (Exception e) {
            logger.error("Erreur lors de la sauvegarde du BombOperationScenario", e);
            throw new RuntimeException("Erreur lors de la sauvegarde du scénario Opération Bombe", e);
        }
    }
}
