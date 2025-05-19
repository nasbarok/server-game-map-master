package com.airsoft.gamemapmaster.scenario.bomboperation.service.impl;
import com.airsoft.gamemapmaster.model.Scenario;
import com.airsoft.gamemapmaster.scenario.bomboperation.exception.BombOperationException;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombOperationScenario;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombSite;
import com.airsoft.gamemapmaster.scenario.bomboperation.repository.BombOperationScenarioRepository;
import com.airsoft.gamemapmaster.scenario.bomboperation.repository.BombSiteRepository;
import com.airsoft.gamemapmaster.scenario.bomboperation.service.BombOperationScenarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BombOperationScenarioServiceImpl implements BombOperationScenarioService {

    private static final Logger logger = LoggerFactory.getLogger(BombOperationScenarioServiceImpl.class);

    @Autowired
    private BombOperationScenarioRepository bombOperationScenarioRepository;

    @Autowired
    private BombSiteRepository bombSiteRepository;

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
        List<BombOperationScenario> scenarios = bombOperationScenarioRepository.findByScenarioId(scenarioId);

        if (scenarios.isEmpty()) {
            logger.error("Aucun scénario d'Opération Bombe trouvé pour le scénario ID: {}", scenarioId);
            throw new BombOperationException.ScenarioNotFoundException(scenarioId);
        }

        return scenarios.get(0);
    }

    @Override
    public BombOperationScenario getActiveBombOperationScenario(Long scenarioId) {
        logger.info("Récupération du scénario d'Opération Bombe actif pour le scénario ID: {}", scenarioId);
        return bombOperationScenarioRepository.findByScenarioIdAndActiveTrue(scenarioId)
                .orElse(null);
    }

    @Override
    @Transactional
    public BombOperationScenario activateBombOperationScenario(Long id) {
        logger.info("Activation du scénario d'Opération Bombe ID: {}", id);

        BombOperationScenario bombOperationScenario = getBombOperationScenarioById(id);

        // Désactiver tous les autres scénarios pour ce scénario de base
        List<BombOperationScenario> scenarios = bombOperationScenarioRepository.findByScenarioId(bombOperationScenario.getScenario().getId());
        for (BombOperationScenario scenario : scenarios) {
            if (!scenario.getId().equals(id) && scenario.getActive()) {
                scenario.setActive(false);
                bombOperationScenarioRepository.save(scenario);
                logger.info("Scénario d'Opération Bombe désactivé: {}", scenario.getId());
            }
        }

        // Activer ce scénario
        bombOperationScenario.setActive(true);
        bombOperationScenario = bombOperationScenarioRepository.save(bombOperationScenario);
        logger.info("Scénario d'Opération Bombe activé: {}", bombOperationScenario.getId());

        return bombOperationScenario;
    }

    @Override
    @Transactional
    public BombOperationScenario deactivateBombOperationScenario(Long id) {
        logger.info("Désactivation du scénario d'Opération Bombe ID: {}", id);

        BombOperationScenario bombOperationScenario = getBombOperationScenarioById(id);
        bombOperationScenario.setActive(false);
        bombOperationScenario = bombOperationScenarioRepository.save(bombOperationScenario);
        logger.info("Scénario d'Opération Bombe désactivé: {}", bombOperationScenario.getId());

        return bombOperationScenario;
    }

    @Override
    @Transactional
    public void deleteBombOperationScenario(Long id) {
        logger.info("Suppression du scénario d'Opération Bombe ID: {}", id);

        BombOperationScenario bombOperationScenario = getBombOperationScenarioById(id);
        bombOperationScenarioRepository.delete(bombOperationScenario);
        logger.info("Scénario d'Opération Bombe supprimé: {}", id);
    }

    @Override
    @Transactional
    public BombSite addBombSite(Long scenarioId, String name, Double latitude, Double longitude, Double radius) {
        logger.info("Ajout d'un site de bombe au scénario d'Opération Bombe ID: {}", scenarioId);

        BombOperationScenario bombOperationScenario = getBombOperationScenarioById(scenarioId);

        BombSite bombSite = new BombSite();
        bombSite.setName(name);
        bombSite.setLatitude(latitude);
        bombSite.setLongitude(longitude);

        if (radius != null) {
            bombSite.setRadius(radius);
        }

        bombSite.setBombOperationScenario(bombOperationScenario);

        bombSite = bombSiteRepository.save(bombSite);
        logger.info("Site de bombe ajouté avec l'ID: {}", bombSite.getId());

        return bombSite;
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
    public List<BombSite> getBombSitesByScenarioId(Long scenarioId) {
        logger.info("Récupération des sites de bombe pour le scénario d'Opération Bombe ID: {}", scenarioId);
        return bombSiteRepository.findByBombOperationScenarioId(scenarioId);
    }

    @Override
    @Transactional
    public void deleteBombSite(Long siteId) {
        logger.info("Suppression du site de bombe ID: {}", siteId);

        BombSite bombSite = getBombSiteById(siteId);
        bombSiteRepository.delete(bombSite);
        logger.info("Site de bombe supprimé: {}", siteId);
    }
}
