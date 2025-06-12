package com.airsoft.gamemapmaster.scenario.bomboperation.service.impl;

import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombSite;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombSiteSessionState;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.BombSiteStatus;
import com.airsoft.gamemapmaster.scenario.bomboperation.repository.BombSiteSessionStateRepository;
import com.airsoft.gamemapmaster.scenario.bomboperation.service.BombSiteSessionStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implémentation du service pour la gestion des états des sites de bombe dans les sessions
 */
@Service
@Transactional
public class BombSiteSessionStateServiceImpl implements BombSiteSessionStateService {
    
    private static final Logger logger = LoggerFactory.getLogger(BombSiteSessionStateServiceImpl.class);
    
    @Autowired
    private BombSiteSessionStateRepository bombSiteSessionStateRepository;
    
    @Override
    public List<BombSiteSessionState> createSessionStatesFromBombSites(Long gameSessionId, List<BombSite> bombSites) {
        logger.info("Création des états de session pour {} sites de bombe (session: {})", bombSites.size(), gameSessionId);
        
        List<BombSiteSessionState> sessionStates = new ArrayList<>();
        
        for (BombSite bombSite : bombSites) {
            BombSiteSessionState sessionState = new BombSiteSessionState(gameSessionId, bombSite);
            sessionStates.add(sessionState);
            logger.debug("État créé pour le site '{}' (ID original: {})", bombSite.getName(), bombSite.getId());
        }
        
        List<BombSiteSessionState> savedStates = bombSiteSessionStateRepository.saveAll(sessionStates);
        logger.info("✅ {} états de sites sauvegardés pour la session {}", savedStates.size(), gameSessionId);
        
        return savedStates;
    }
    
    @Override
    public List<BombSiteSessionState> selectAndActivateRandomSites(Long gameSessionId, int numberOfSitesToActivate) {
        logger.info("Sélection et activation de {} sites aléatoires pour la session {}", numberOfSitesToActivate, gameSessionId);
        
        // Récupérer tous les sites inactifs de la session
        List<BombSiteSessionState> inactiveSites = bombSiteSessionStateRepository
                .findByGameSessionIdAndStatus(gameSessionId, BombSiteStatus.INACTIVE);
        
        if (inactiveSites.isEmpty()) {
            logger.warn("Aucun site inactif trouvé pour la session {}", gameSessionId);
            return new ArrayList<>();
        }
        
        if (numberOfSitesToActivate > inactiveSites.size()) {
            logger.warn("Nombre de sites demandés ({}) supérieur au nombre de sites disponibles ({}). Activation de tous les sites disponibles.", 
                    numberOfSitesToActivate, inactiveSites.size());
            numberOfSitesToActivate = inactiveSites.size();
        }
        
        // Mélanger la liste et prendre les premiers éléments
        Collections.shuffle(inactiveSites);
        List<BombSiteSessionState> sitesToActivate = inactiveSites.subList(0, numberOfSitesToActivate);
        
        // Activer les sites sélectionnés
        List<BombSiteSessionState> activatedSites = new ArrayList<>();
        for (BombSiteSessionState site : sitesToActivate) {
            site.activate();
            BombSiteSessionState savedSite = bombSiteSessionStateRepository.save(site);
            activatedSites.add(savedSite);
            logger.debug("Site '{}' activé (ID: {})", site.getName(), site.getId());
        }
        
        logger.info("✅ {} sites activés avec succès pour la session {}", activatedSites.size(), gameSessionId);
        return activatedSites;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BombSiteSessionState> getAllSessionStates(Long gameSessionId) {
        return bombSiteSessionStateRepository.findByGameSessionId(gameSessionId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BombSiteSessionState> getActiveSites(Long gameSessionId) {
        return bombSiteSessionStateRepository.findActiveSitesByGameSessionId(gameSessionId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BombSiteSessionState> getArmedSites(Long gameSessionId) {
        return bombSiteSessionStateRepository.findArmedSitesByGameSessionId(gameSessionId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BombSiteSessionState> getExplodedSites(Long gameSessionId) {
        return bombSiteSessionStateRepository.findExplodedSitesByGameSessionId(gameSessionId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BombSiteSessionState> getDisarmedSites(Long gameSessionId) {
        return bombSiteSessionStateRepository.findDisarmedSitesByGameSessionId(gameSessionId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<BombSiteSessionState> findByGameSessionAndOriginalSite(Long gameSessionId, Long originalBombSiteId) {
        return bombSiteSessionStateRepository.findByGameSessionIdAndOriginalBombSiteId(gameSessionId, originalBombSiteId);
    }
    
    @Override
    public BombSiteSessionState activateSite(Long gameSessionId, Long originalBombSiteId) {
        logger.info("Activation du site {} pour la session {}", originalBombSiteId, gameSessionId);
        
        Optional<BombSiteSessionState> optionalSite = findByGameSessionAndOriginalSite(gameSessionId, originalBombSiteId);
        if (optionalSite.isEmpty()) {
            throw new IllegalArgumentException("Site non trouvé: " + originalBombSiteId + " pour la session " + gameSessionId);
        }
        
        BombSiteSessionState site = optionalSite.get();
        site.activate();
        BombSiteSessionState savedSite = bombSiteSessionStateRepository.save(site);
        
        logger.info("✅ Site '{}' activé avec succès", site.getName());
        return savedSite;
    }
    
    @Override
    public BombSiteSessionState deactivateSite(Long gameSessionId, Long originalBombSiteId) {
        logger.info("Désactivation du site {} pour la session {}", originalBombSiteId, gameSessionId);
        
        Optional<BombSiteSessionState> optionalSite = findByGameSessionAndOriginalSite(gameSessionId, originalBombSiteId);
        if (optionalSite.isEmpty()) {
            throw new IllegalArgumentException("Site non trouvé: " + originalBombSiteId + " pour la session " + gameSessionId);
        }
        
        BombSiteSessionState site = optionalSite.get();
        site.deactivate();
        BombSiteSessionState savedSite = bombSiteSessionStateRepository.save(site);
        
        logger.info("✅ Site '{}' désactivé avec succès", site.getName());
        return savedSite;
    }
    
    @Override
    public BombSiteSessionState armBomb(Long gameSessionId, Long bombSiteId, Long userId, Integer bombTimerSeconds) {
        logger.info("Armement de la bombe sur le site {} par l'utilisateur {} (session: {})",
                bombSiteId, userId, gameSessionId);

        BombSiteSessionState site = bombSiteSessionStateRepository.findByGameSessionIdAndOriginalBombSiteId(gameSessionId, bombSiteId)
                .orElseThrow(() -> new IllegalArgumentException("Site de bombe non trouvé: " + bombSiteId));
        
        if (!site.isActive()) {
            throw new IllegalStateException("Le site '" + site.getName() + "' n'est pas actif et ne peut pas être armé");
        }
        
        site.arm(userId, bombTimerSeconds);
        BombSiteSessionState savedSite = bombSiteSessionStateRepository.save(site);
        
        logger.info("✅ Bombe armée sur le site '{}' avec un timer de {} secondes", site.getName(), bombTimerSeconds);
        return savedSite;
    }
    
    @Override
    public BombSiteSessionState disarmBomb(Long gameSessionId, Long bombSiteId, Long userId) {
        logger.info("Désarmement de la bombe sur le site {} par l'utilisateur {} (session: {})",
                bombSiteId, userId, gameSessionId);

        BombSiteSessionState site = bombSiteSessionStateRepository.findByGameSessionIdAndOriginalBombSiteId(gameSessionId, bombSiteId)
                .orElseThrow(() -> new IllegalArgumentException("Site de bombe non trouvé: " + bombSiteId));
        
        if (!site.isArmed()) {
            throw new IllegalStateException("Le site '" + site.getName() + "' n'est pas armé et ne peut pas être désarmé");
        }
        
        site.disarm(userId);
        BombSiteSessionState savedSite = bombSiteSessionStateRepository.save(site);
        
        logger.info("✅ Bombe désarmée sur le site '{}'", site.getName());
        return savedSite;
    }
    
    @Override
    public BombSiteSessionState explodeBomb(Long gameSessionId, Long originalBombSiteId) {
        logger.info("Explosion de la bombe sur le site {} (session: {})", originalBombSiteId, gameSessionId);
        
        Optional<BombSiteSessionState> optionalSite = findByGameSessionAndOriginalSite(gameSessionId, originalBombSiteId);
        if (optionalSite.isEmpty()) {
            throw new IllegalArgumentException("Site non trouvé: " + originalBombSiteId + " pour la session " + gameSessionId);
        }
        
        BombSiteSessionState site = optionalSite.get();
        site.explode();
        BombSiteSessionState savedSite = bombSiteSessionStateRepository.save(site);
        
        logger.info("💥 Bombe explosée sur le site '{}'", site.getName());
        return savedSite;
    }
    
    @Override
    public List<BombSiteSessionState> checkAndExplodeExpiredBombs(Long gameSessionId) {
        logger.debug("Vérification des bombes expirées pour la session {}", gameSessionId);
        
        List<BombSiteSessionState> expiredBombs = bombSiteSessionStateRepository
                .findSitesThatShouldHaveExploded(LocalDateTime.now())
                .stream()
                .filter(site -> site.getGameSessionId().equals(gameSessionId))
                .collect(Collectors.toList());
        
        List<BombSiteSessionState> explodedSites = new ArrayList<>();
        
        for (BombSiteSessionState site : expiredBombs) {
            site.explode();
            BombSiteSessionState savedSite = bombSiteSessionStateRepository.save(site);
            explodedSites.add(savedSite);
            logger.info("💥 Bombe expirée explosée automatiquement sur le site '{}'", site.getName());
        }
        
        if (!explodedSites.isEmpty()) {
            logger.info("✅ {} bombes expirées ont explosé automatiquement pour la session {}", 
                    explodedSites.size(), gameSessionId);
        }
        
        return explodedSites;
    }
    
    @Override
    public void deleteAllSessionStates(Long gameSessionId) {
        logger.info("Suppression de tous les états de sites pour la session {}", gameSessionId);
        bombSiteSessionStateRepository.deleteByGameSessionId(gameSessionId);
        logger.info("✅ États de sites supprimés pour la session {}", gameSessionId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveSites(Long gameSessionId) {
        return bombSiteSessionStateRepository.hasActiveSites(gameSessionId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasArmedSites(Long gameSessionId) {
        return bombSiteSessionStateRepository.hasArmedSites(gameSessionId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<BombSiteStatus, Long> getSiteStatistics(Long gameSessionId) {
        List<Object[]> results = bombSiteSessionStateRepository.countSitesByStatusForSession(gameSessionId);
        
        Map<BombSiteStatus, Long> statistics = new HashMap<>();
        // Initialiser avec des valeurs par défaut
        for (BombSiteStatus status : BombSiteStatus.values()) {
            statistics.put(status, 0L); // Valeur par défaut pour chaque statut
        }

        // Remplir avec les vraies valeurs
        for (Object[] result : results) {
            BombSiteStatus status = (BombSiteStatus) result[0];
            Long count = (Long) result[1];
            statistics.put(status, count);
        }
        
        return statistics;
    }

    @Override
    public BombSiteSessionState getBombSiteStatus(Long bombSiteStatusId) {
        logger.info("Récupération du statut de site de bombe pour l'ID {}", bombSiteStatusId);

        Optional<BombSiteSessionState> optionalBombSiteSessionState =   bombSiteSessionStateRepository.findById(bombSiteStatusId);
        if (optionalBombSiteSessionState.isEmpty()) {
            logger.warn("Statut de site de bombe non trouvé pour l'ID {}", bombSiteStatusId);
            return null; // ou lancer une exception si nécessaire
        }

        BombSiteSessionState status = optionalBombSiteSessionState.get();
        logger.info("✅ Statut de site de bombe récupéré: {}", status);
        return status;
    }
}

