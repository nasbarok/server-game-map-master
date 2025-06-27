package com.airsoft.gamemapmaster.scenario.bomboperation.service.impl;

import com.airsoft.gamemapmaster.model.GameSession;
import com.airsoft.gamemapmaster.model.GameSessionParticipant;
import com.airsoft.gamemapmaster.repository.GameSessionRepository;
import com.airsoft.gamemapmaster.repository.TeamRepository;
import com.airsoft.gamemapmaster.repository.UserRepository;
import com.airsoft.gamemapmaster.scenario.bomboperation.dto.BombOperationHistoryDto;
import com.airsoft.gamemapmaster.scenario.bomboperation.dto.BombSiteHistoryDto;
import com.airsoft.gamemapmaster.scenario.bomboperation.model.*;
import com.airsoft.gamemapmaster.scenario.bomboperation.repository.BombOperationSessionRepository;
import com.airsoft.gamemapmaster.scenario.bomboperation.repository.BombOperationTeamRoleRepository;
import com.airsoft.gamemapmaster.scenario.bomboperation.repository.BombSiteSessionStateRepository;
import com.airsoft.gamemapmaster.scenario.bomboperation.service.BombSiteSessionStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.airsoft.gamemapmaster.scenario.bomboperation.model.BombSiteStatus.*;

/**
 * Implémentation du service pour la gestion des états des sites de bombe dans les sessions
 */
@Service
@Transactional
public class BombSiteSessionStateServiceImpl implements BombSiteSessionStateService {
    
    private static final Logger logger = LoggerFactory.getLogger(BombSiteSessionStateServiceImpl.class);
    
    @Autowired
    private BombSiteSessionStateRepository bombSiteSessionStateRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private BombOperationTeamRoleRepository teamRoleRepository;
    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private BombOperationSessionRepository bombOperationSessionRepository;
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
                .findByGameSessionIdAndStatus(gameSessionId, INACTIVE);
        
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
        List<BombSiteSessionState> bombSiteSessionStates = bombSiteSessionStateRepository
                .findByGameSessionIdAndStatusIn(gameSessionId,
                        List.of(ACTIVE));

        return bombSiteSessionStates;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BombSiteSessionState> getArmedSites(Long gameSessionId) {
        List<BombSiteSessionState> armedOrDisarmed = bombSiteSessionStateRepository
                .findByGameSessionIdAndStatusIn(gameSessionId,
                        List.of(ARMED, BombSiteStatus.DISARMED));

        List<BombSiteSessionState> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (BombSiteSessionState state : armedOrDisarmed) {
            // si ARMED, vérifier qu'elle n'a pas dépassé son timer
            if (state.getStatus() == ARMED) {
                LocalDateTime armedAt = state.getArmedAt();
                Integer timer = state.getBombTimer();

                if (armedAt != null && timer != null) {
                    LocalDateTime expectedExplosion = armedAt.plusSeconds(timer);
                    if (now.isAfter(expectedExplosion)) {
                        // Bombe expirée, ne pas la renvoyer
                        continue;
                    }
                }
            }
            result.add(state);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BombSiteSessionState> getExplodedSites(Long gameSessionId) {
        List<BombSiteSessionState> allStates = bombSiteSessionStateRepository
                .findByGameSessionIdAndStatusIn(gameSessionId,
                        List.of(BombSiteStatus.EXPLODED, ARMED));

        List<BombSiteSessionState> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (BombSiteSessionState state : allStates) {
            if (state.getStatus() == BombSiteStatus.EXPLODED) {
                result.add(state);
                continue;
            }

            if (state.getStatus() == ARMED) {
                LocalDateTime armedAt = state.getArmedAt();
                Integer bombTimer = state.getBombTimer();

                if (armedAt != null && bombTimer != null) {
                    LocalDateTime explosionTime = armedAt.plusSeconds(bombTimer);
                    if (now.isAfter(explosionTime)) {
                        result.add(state);
                    }
                }
            }
        }

        return result;
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
    public BombSiteSessionState armBomb(Long gameSessionId, Long bombSiteId, Long userId,LocalDateTime actionTime, Integer bombTimerSeconds) {
        logger.info("Armement de la bombe sur le site {} par l'utilisateur {} (session: {})",
                bombSiteId, userId, gameSessionId);

        BombSiteSessionState site = bombSiteSessionStateRepository.findByGameSessionIdAndOriginalBombSiteId(gameSessionId, bombSiteId)
                .orElseThrow(() -> new IllegalArgumentException("Site de bombe non trouvé: " + bombSiteId));
        
        if (!site.isActive()) {
            throw new IllegalStateException("Le site '" + site.getName() + "' n'est pas actif et ne peut pas être armé");
        }
        
        site.arm(userId, bombTimerSeconds,actionTime);
        BombSiteSessionState savedSite = bombSiteSessionStateRepository.save(site);
        
        logger.info("✅ Bombe armée sur le site '{}' avec un timer de {} secondes", site.getName(), bombTimerSeconds);
        return savedSite;
    }
    
    @Override
    public BombSiteSessionState disarmBomb(Long gameSessionId, Long bombSiteId, Long userId,LocalDateTime actionTime) {
        logger.info("Désarmement de la bombe sur le site {} par l'utilisateur {} (session: {})",
                bombSiteId, userId, gameSessionId);

        BombSiteSessionState site = bombSiteSessionStateRepository.findByGameSessionIdAndOriginalBombSiteId(gameSessionId, bombSiteId)
                .orElseThrow(() -> new IllegalArgumentException("Site de bombe non trouvé: " + bombSiteId));
        
        if (!site.isArmed()) {
            throw new IllegalStateException("Le site '" + site.getName() + "' n'est pas armé et ne peut pas être désarmé");
        }
        
        site.disarm(userId,actionTime);
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


    @Override
    public BombOperationHistoryDto getSessionHistory(Long gameSessionId) {

        GameSession gameSession = gameSessionRepository.findById(gameSessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        BombOperationSession bombOperationSession = bombOperationSessionRepository.findByGameSessionId(gameSessionId)
                .orElseThrow(() -> new RuntimeException("BombOperationSession not found"));

        BombOperationScenario bombOperationScenario = bombOperationSession.getBombOperationScenario();
        List<BombSiteSessionState> allSites = getAllSessionStates(gameSessionId);
        Map<BombSiteStatus, Long> statusCounts = allSites.stream()
                .collect(Collectors.groupingBy(BombSiteSessionState::getStatus, Collectors.counting()));


        BombOperationHistoryDto.BombOperationStatsDto stats = new BombOperationHistoryDto.BombOperationStatsDto();
        stats.setGameSessionId(gameSessionId);
        stats.setTotalSites(allSites.size());
        stats.setActivatedSites(statusCounts.getOrDefault(ACTIVE, 0L).intValue());
        stats.setArmedSites(statusCounts.getOrDefault(ARMED, 0L).intValue());
        stats.setDisarmedSites(statusCounts.getOrDefault(BombSiteStatus.DISARMED, 0L).intValue());
        stats.setExplodedSites(statusCounts.getOrDefault(BombSiteStatus.EXPLODED, 0L).intValue());

        int exploded = stats.getExplodedSites();
        int disarmed = stats.getDisarmedSites();

        if (exploded > disarmed) {
            stats.setResult("TERRORISTS_WIN");
            stats.setWinningTeam("ATTACK");
            stats.setWinCondition("MORE_EXPLOSIONS");
        } else if (disarmed > exploded) {
            stats.setResult("COUNTER_TERRORISTS_WIN");
            stats.setWinningTeam("DEFENSE");
            stats.setWinCondition("MORE_DISARMS");
        } else {
            stats.setResult("DRAW");
            stats.setWinningTeam("NONE");
            stats.setWinCondition("EQUAL_OUTCOME");
        }

        stats.setSessionDurationMinutes(gameSession.getDurationMinutes().longValue());
        stats.setTimeline(getSessionTimeline(gameSessionId));

        // 🧩 Créer et remplir l’objet principal complet
        BombOperationHistoryDto dto = new BombOperationHistoryDto();
        dto.setGameSessionId(gameSessionId);
        dto.setSessionStartTime(gameSession.getStartTime());
        dto.setSessionEndTime(gameSession.getEndTime());

        dto.setScenarioName(bombOperationScenario.getScenario().getName());
        dto.setBombTimer(bombOperationScenario.getBombTimer());
        dto.setDefuseTime(bombOperationScenario.getDefuseTime());
        dto.setArmingTime(bombOperationScenario.getArmingTime());
        dto.setActiveSites(bombOperationScenario.getActiveSites());

        dto.setBombSitesHistory(getBombSitesHistory(gameSessionId));
        dto.setTimeline(stats.getTimeline());
        dto.setFinalStats(stats);

        return dto;
    }

    @Override
    public List<BombSiteHistoryDto> getBombSitesHistory(Long gameSessionId) {
        List<BombSiteSessionState> allSites = getAllSessionStates(gameSessionId);

        return allSites.stream()
                .map(this::convertToHistoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BombOperationHistoryDto.BombEventDto> getSessionTimeline(Long gameSessionId) {
        List<BombSiteSessionState> allSites = getAllSessionStates(gameSessionId);
        Map<Long, String> userNames = new HashMap<>();
        Map<Long, String> userTeamRoles = new HashMap<>();

        // 📌 Préparer les rôles
        List<BombOperationTeamRole> roles = teamRoleRepository.findByGameSessionId(gameSessionId);
        Map<Long, String> teamIdToRole = roles.stream()
                .collect(Collectors.toMap(BombOperationTeamRole::getTeamId, BombOperationTeamRole::getRole));

        List<GameSessionParticipant> participants = gameSessionRepository.findById(gameSessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"))
                .getParticipants();

        Map<Long, Long> userToTeam = participants.stream()
                .collect(Collectors.toMap(p -> p.getUser().getId(), p -> p.getTeam().getId()));

        for (GameSessionParticipant p : participants) {
            Long userId = p.getUser().getId();
            userNames.put(userId, p.getUser().getUsername());
            Long teamId = p.getTeam().getId();
            String role = teamIdToRole.getOrDefault(teamId, "unknown");
            userTeamRoles.put(userId, role);
        }

        // ✅ Charger le scénario associé à la session
        BombOperationScenario bombOperationScenario = bombOperationSessionRepository
                .findByGameSessionId(gameSessionId)
                .map(BombOperationSession::getBombOperationScenario)
                .orElseThrow(() -> new RuntimeException("BombOperationScenario not found"));

        int bombTimer = bombOperationScenario.getBombTimer();
        int defuseTime = bombOperationScenario.getDefuseTime();

        List<BombOperationHistoryDto.BombEventDto> events = new ArrayList<>();

        for (BombSiteSessionState site : allSites) {
            // 🔁 SITE_ACTIVATED
            if (site.getActivatedAt() != null) {
                BombOperationHistoryDto.BombEventDto e = new BombOperationHistoryDto.BombEventDto();
                e.setTimestamp(site.getActivatedAt());
                e.setEventType("SITE_ACTIVATED");
                e.setSiteName(site.getName());
                e.setDescription("Site " + site.getName() + " activé");
                e.setTimeRemainingSeconds(null);
                events.add(e);
            }

            // 🔁 BOMB_ARMED
            if (site.getArmedAt() != null) {
                Long uid = site.getArmedByUserId();
                BombOperationHistoryDto.BombEventDto e = new BombOperationHistoryDto.BombEventDto();
                e.setTimestamp(site.getArmedAt());
                e.setEventType("BOMB_ARMED");
                e.setSiteName(site.getName());
                e.setUserId(uid);
                e.setPlayerName(userNames.getOrDefault(uid, "Inconnu"));
                e.setTeamRole(userTeamRoles.getOrDefault(uid, "unknown"));
                e.setDescription("Bombe armée sur le site " + site.getName());
                e.setTimeRemainingSeconds(bombTimer); // 💣 Valeur directe depuis scénario
                events.add(e);
            }

            // 🔁 BOMB_DISARMED
            if (site.getDisarmedAt() != null) {
                Long uid = site.getDisarmedByUserId();
                BombOperationHistoryDto.BombEventDto e = new BombOperationHistoryDto.BombEventDto();
                e.setTimestamp(site.getDisarmedAt());
                e.setEventType("BOMB_DISARMED");
                e.setSiteName(site.getName());
                e.setUserId(uid);
                e.setPlayerName(userNames.getOrDefault(uid, "Inconnu"));
                e.setTeamRole(userTeamRoles.getOrDefault(uid, "unknown"));
                e.setDescription("Bombe désarmée sur le site " + site.getName());

                if (site.getExpectedExplosionAt() != null) {
                    long remaining = Duration.between(site.getDisarmedAt(), site.getExpectedExplosionAt()).getSeconds();
                    e.setTimeRemainingSeconds((int) Math.max(0, remaining)); // ⏱️ temps réel
                } else {
                    e.setTimeRemainingSeconds(defuseTime); // fallback si non calculable
                }

                events.add(e);
            }

            // 🔁 BOMB_EXPLODED
            if (site.getExplodedAt() != null) {
                BombOperationHistoryDto.BombEventDto e = new BombOperationHistoryDto.BombEventDto();
                e.setTimestamp(site.getExplodedAt());
                e.setEventType("BOMB_EXPLODED");
                e.setSiteName(site.getName());
                e.setDescription("Bombe explosée sur le site " + site.getName());
                e.setTimeRemainingSeconds(0); // 💥
                events.add(e);
            }
        }

        events.sort(Comparator.comparing(BombOperationHistoryDto.BombEventDto::getTimestamp));
        return events;
    }

    @Override
    public List<BombSiteHistoryDto> getSitesStateAtTime(Long gameSessionId, LocalDateTime timestamp) {
        List<BombSiteSessionState> allSites = getAllSessionStates(gameSessionId);

        return allSites.stream()
                .map(site -> convertToHistoryDtoAtTime(site, timestamp))
                .collect(Collectors.toList());
    }

    // ===== MÉTHODES UTILITAIRES =====

    private BombSiteStatus convertSessionStatusToSiteStatus(BombSiteSessionState sessionStatus) {
        if (sessionStatus.equals(INACTIVE)) {
            return ACTIVE; // Mapping par défaut
        } else if (sessionStatus.equals(ACTIVE)) {
            return ACTIVE;
        } else if (sessionStatus.equals(ARMED)) {
            return ARMED;
        } else if (sessionStatus.equals(DISARMED)) {
            return DISARMED;
        } else if (sessionStatus.equals(EXPLODED)) {
            return EXPLODED;
        }
        return ACTIVE;
    }

    private BombSiteHistoryDto convertToHistoryDto(BombSiteSessionState site) {
        BombSiteHistoryDto dto = new BombSiteHistoryDto();
        dto.setId(site.getId());
        dto.setOriginalBombSiteId(site.getOriginalBombSiteId());
        dto.setName(site.getName());
        dto.setLatitude(site.getLatitude());
        dto.setLongitude(site.getLongitude());
        dto.setRadius(site.getRadius());
        dto.setStatus(site.getStatus().name());
        dto.setCreatedAt(site.getCreatedAt());
        dto.setActivatedAt(site.getActivatedAt());
        dto.setArmedAt(site.getArmedAt());
        dto.setArmedByUserId(site.getArmedByUserId());
        dto.setDisarmedAt(site.getDisarmedAt());
        dto.setDisarmedByUserId(site.getDisarmedByUserId());
        dto.setExplodedAt(site.getExplodedAt());
        return dto;
    }

    private BombSiteHistoryDto convertToHistoryDtoAtTime(BombSiteSessionState site, LocalDateTime timestamp) {
        BombSiteHistoryDto dto = convertToHistoryDto(site);

        // Déterminer l'état du site au moment donné
        if (site.getCreatedAt() != null && timestamp.isBefore(site.getCreatedAt())) {
            dto.setStatus("NOT_CREATED");
        } else if (site.getActivatedAt() != null && timestamp.isBefore(site.getActivatedAt())) {
            dto.setStatus("INACTIVE");
        } else if (site.getArmedAt() != null && timestamp.isBefore(site.getArmedAt())) {
            dto.setStatus("ACTIVE");
        } else if (site.getDisarmedAt() != null && timestamp.isBefore(site.getDisarmedAt())) {
            dto.setStatus("ARMED");
        } else if (site.getExplodedAt() != null && timestamp.isBefore(site.getExplodedAt())) {
            dto.setStatus("ARMED");
        } else {
            // Utiliser l'état actuel
            dto.setStatus(site.getStatus().name());
        }

        return dto;
    }
}

