package com.airsoft.gamemapmaster.scenario.treasurehunt.service.impl;

import com.airsoft.gamemapmaster.model.Scenario;
import com.airsoft.gamemapmaster.model.Team;
import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.scenario.treasurehunt.model.*;
import com.airsoft.gamemapmaster.scenario.treasurehunt.repository.TreasureFoundRepository;
import com.airsoft.gamemapmaster.scenario.treasurehunt.repository.TreasureHuntScenarioRepository;
import com.airsoft.gamemapmaster.scenario.treasurehunt.repository.TreasureHuntScoreRepository;
import com.airsoft.gamemapmaster.scenario.treasurehunt.repository.TreasureRepository;
import com.airsoft.gamemapmaster.scenario.treasurehunt.service.TreasureHuntService;
import com.airsoft.gamemapmaster.scenario.treasurehunt.websocket.TreasureHuntWebSocketHandler;
import com.airsoft.gamemapmaster.service.GameSessionService;
import com.airsoft.gamemapmaster.service.TeamService;
import com.airsoft.gamemapmaster.service.UserService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TreasureHuntServiceImpl implements TreasureHuntService {
    private static final Logger logger = LoggerFactory.getLogger(TreasureHuntServiceImpl.class);

    @Autowired
    private TreasureHuntScenarioRepository treasureHuntScenarioRepository;

    @Autowired
    private TreasureRepository treasureRepository;

    @Autowired
    private TreasureFoundRepository treasureFoundRepository;

    @Autowired
    private TreasureHuntScoreRepository treasureHuntScoreRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TeamService teamService;

    @Autowired
    @Lazy
    private GameSessionService gameSessionService;

    @Autowired
    private TreasureHuntWebSocketHandler treasureHuntWebSocketHandler;

    @Override
    public TreasureHuntScenario saveTreasureHuntScenario(TreasureHuntScenario treasureHuntScenario) {
        return treasureHuntScenarioRepository.save(treasureHuntScenario);
    }

    @Override
    public Optional<TreasureHuntScenario> findById(Long id) {
        return treasureHuntScenarioRepository.findById(id);
    }

    @Override
    public Optional<TreasureHuntScenario> findByScenarioId(Long scenarioId) {
        return treasureHuntScenarioRepository.findByScenarioId(scenarioId);
    }

    @Override
    @Transactional
    public void lockScores(Long treasureHuntScenarioId, boolean locked) {
        treasureHuntScenarioRepository.findById(treasureHuntScenarioId).ifPresent(scenario -> {
            scenario.setScoresLocked(locked);
            treasureHuntScenarioRepository.save(scenario);

            // Notifier les clients via WebSocket
            Map<String, Object> scoreboard = getScoreboard(treasureHuntScenarioId, gameSessionService.getCurrentGameSessionId());
            treasureHuntWebSocketHandler.updateScoreboard(scenario.getScenario().getId(), scoreboard);
        });
    }

    @Override
    @Transactional
    public void resetScores(Long treasureHuntScenarioId, Long gameSessionId) {
        // Supprimer tous les scores pour ce scénario et cette session
        List<TreasureHuntScore> scores = treasureHuntScoreRepository.findByTreasureHuntScenarioIdAndGameSessionId(
                treasureHuntScenarioId, gameSessionId);
        treasureHuntScoreRepository.deleteAll(scores);

        // Supprimer tous les trésors trouvés pour ce scénario et cette session
        Optional<TreasureHuntScenario> scenarioOpt = treasureHuntScenarioRepository.findById(treasureHuntScenarioId);
        if (scenarioOpt.isPresent()) {
            TreasureHuntScenario scenario = scenarioOpt.get();
            List<Treasure> treasures = treasureRepository.findByTreasureHuntScenarioId(treasureHuntScenarioId);

            for (Treasure treasure : treasures) {
                List<TreasureFound> found = treasureFoundRepository.findByTreasureIdAndGameSessionId(
                        treasure.getId(), gameSessionId);
                treasureFoundRepository.deleteAll(found);
            }

            // Notifier les clients via WebSocket
            Map<String, Object> scoreboard = getScoreboard(treasureHuntScenarioId, gameSessionId);
            treasureHuntWebSocketHandler.updateScoreboard(scenario.getScenario().getId(), scoreboard);
        }
    }

    @Override
    @Transactional
    public void activateScenario(Long treasureHuntScenarioId, boolean active) {
        treasureHuntScenarioRepository.findById(treasureHuntScenarioId).ifPresent(scenario -> {
            scenario.setActive(active);
            treasureHuntScenarioRepository.save(scenario);
        });
    }

    @Override
    public List<TreasureHuntScenario> findActiveScenarios() {
        return treasureHuntScenarioRepository.findAllActive();
    }

    @Override
    public Treasure saveTreasure(Treasure treasure) {
        return treasureRepository.save(treasure);
    }

    @Override
    public Optional<Treasure> findTreasureById(Long id) {
        return treasureRepository.findById(id);
    }

    @Override
    public List<Treasure> findTreasuresByTreasureHuntId(Long treasureHuntId) {
        return treasureRepository.findByTreasureHuntScenarioIdOrderByOrderNumberAsc(treasureHuntId);
    }

    @Override
    public Optional<Treasure> findTreasureByQrCode(String qrCode) {
        return treasureRepository.findByQrCode(qrCode);
    }

    @Override
    @Transactional
    public List<Treasure> createTreasuresBatch(Long treasureHuntId, int count, int defaultValue, String defaultSymbol) {
        logger.info("🔄 Démarrage de la synchronisation de {} trésors pour le TreasureHuntScenario ID: {}", count, treasureHuntId);

        Optional<TreasureHuntScenario> scenarioOpt = treasureHuntScenarioRepository.findById(treasureHuntId);
        if (!scenarioOpt.isPresent()) {
            logger.warn("❌ TreasureHuntScenario non trouvé pour ID: {}", treasureHuntId);
            throw new IllegalArgumentException("Scénario de chasse au trésor non trouvé: " + treasureHuntId);
        }

        TreasureHuntScenario treasureHuntScenario = scenarioOpt.get();
        List<Treasure> existingTreasures = treasureRepository.findByTreasureHuntScenarioIdOrderByOrderNumberAsc(treasureHuntId);

        int existingCount = existingTreasures.size();

        // 1. Supprimer les trésors en trop
        if (existingCount > count) {
            List<Treasure> treasuresToDelete = existingTreasures.subList(count, existingCount);
            treasureRepository.deleteAll(treasuresToDelete);
            logger.info("🗑️ {} trésors supprimés.", treasuresToDelete.size());
        }

        // 2. Ajouter de nouveaux trésors si besoin
        if (existingCount < count) {
            for (int i = existingCount + 1; i <= count; i++) {
                Treasure treasure = new Treasure();
                treasure.setTreasureHuntScenario(treasureHuntScenario);
                treasure.setName("Trésor " + i);
                treasure.setPoints(defaultValue);
                treasure.setSymbol(defaultSymbol);
                treasure.setOrderNumber(i);

                String qrContent = generateUniqueQRCodeContent(
                        treasureHuntScenario,
                        i,
                        defaultValue
                );

                if (qrContent == null || qrContent.trim().isEmpty()) {
                    logger.error("❌ Échec de génération de QR Code pour Trésor {}. Annulation.", i);
                    throw new IllegalStateException("Erreur de génération de QR Code pour le trésor " + i);
                }

                treasure.setQrCode(qrContent);
                treasureRepository.save(treasure);

                logger.info("✅ Nouveau Trésor {} créé - QRCode: {}", i, treasure.getQrCode());
            }
        }

        // 3. Mettre à jour le TreasureHuntScenario
        treasureHuntScenario.setTotalTreasures(count);
        treasureHuntScenario.setDefaultValue(defaultValue);
        treasureHuntScenario.setDefaultSymbol(defaultSymbol);
        treasureHuntScenarioRepository.save(treasureHuntScenario);

        logger.info("🎯 Synchronisation terminée pour TreasureHuntScenario ID: {}", treasureHuntId);

        return treasureRepository.findByTreasureHuntScenarioIdOrderByOrderNumberAsc(treasureHuntId);
    }

    // Nouvelle fonction qui génère un QR code UNIQUE
    private String generateUniqueQRCodeContent(TreasureHuntScenario scenario, int orderNumber, int points) {
        String baseContent = generateQRCodeContent(
                scenario.getScenario().getName(),
                scenario.getScenario().getId(),
                scenario.getId(),
                orderNumber,
                points
        );

        String qrContent = baseContent;
        int attempt = 1;

        while (treasureRepository.findByQrCode(qrContent).isPresent()) {
            // Si déjà pris, rajoute un suffixe _v2, _v3, etc...
            qrContent = baseContent + "_v" + attempt;
            attempt++;
        }

        return qrContent;
    }

    @Override
    @Transactional
    public Treasure updateTreasure(Long treasureId, String name, Integer points, String symbol) {
        Optional<Treasure> treasureOpt = treasureRepository.findById(treasureId);
        if (!treasureOpt.isPresent()) {
            throw new IllegalArgumentException("Trésor non trouvé: " + treasureId);
        }

        Treasure treasure = treasureOpt.get();

        if (name != null && !name.isEmpty()) {
            treasure.setName(name);
        }

        if (points != null) {
            treasure.setPoints(points);
        }

        if (symbol != null && !symbol.isEmpty()) {
            treasure.setSymbol(symbol);
        }

        // Mettre à jour le QR code si les valeurs ont changé
        String qrContent = generateQRCodeContent(
                treasure.getTreasureHuntScenario().getScenario().getName(),
                treasure.getTreasureHuntScenario().getScenario().getId(),
                treasure.getTreasureHuntScenario().getId(),
                treasure.getOrderNumber(),
                treasure.getPoints()
        );
        treasure.setQrCode(qrContent);

        return treasureRepository.save(treasure);
    }

    @Override
    public TreasureFound saveTreasureFound(TreasureFound treasureFound) {
        return treasureFoundRepository.save(treasureFound);
    }

    @Override
    public List<TreasureFound> findTreasuresFoundByUserId(Long userId) {
        return treasureFoundRepository.findByUserId(userId);
    }

    @Override
    public List<TreasureFound> findTreasuresFoundByTeamId(Long teamId) {
        return treasureFoundRepository.findByTeamId(teamId);
    }

    @Override
    public List<TreasureFound> findTreasuresFoundByGameSessionId(Long gameSessionId) {
        return treasureFoundRepository.findByGameSessionId(gameSessionId);
    }

    @Override
    @Transactional
    public Optional<TreasureFound> recordTreasureFound(String qrCode, Long userId, Long teamId, Long gameSessionId) {
        // Vérifier si le QR code correspond à un trésor
        Optional<Treasure> treasureOpt = treasureRepository.findByQrCode(qrCode);
        if (!treasureOpt.isPresent()) {
            logger.warn("QR code non reconnu: {}", qrCode);
            return Optional.empty();
        }

        Treasure treasure = treasureOpt.get();
        TreasureHuntScenario treasureHuntScenario = treasure.getTreasureHuntScenario();
        Scenario scenario = treasureHuntScenario.getScenario();

        // Vérifier si le scénario est actif
        if (!scenario.isActive()) {
            logger.warn("Le scénario n'est pas actif");
            return Optional.empty();
        }

        // Vérifier si la partie est active
        if (!gameSessionService.isGameSessionActive(gameSessionId)) {
            logger.warn("La partie n'est pas active");
            return Optional.empty();
        }

        // Vérifier si les scores sont verrouillés
        if (treasureHuntScenario.getScoresLocked()) {
            logger.warn("Les scores sont verrouillés pour ce scénario");
            return Optional.empty();
        }

        // Vérifier si l'utilisateur a déjà trouvé ce trésor dans cette session
        Optional<TreasureFound> existingFound = treasureFoundRepository.findByTreasureIdAndUserIdAndGameSessionId(
                treasure.getId(), userId, gameSessionId);
        if (existingFound.isPresent()) {
            logger.warn("L'utilisateur a déjà trouvé ce trésor dans cette session");
            return Optional.empty();
        }

        // Récupérer l'utilisateur et l'équipe
        Optional<User> userOpt = userService.findById(userId);
        if (!userOpt.isPresent()) {
            logger.warn("Utilisateur non trouvé: {}", userId);
            return Optional.empty();
        }

        User user = userOpt.get();
        Team team = null;

        if (teamId != null) {
            Optional<Team> teamOpt = teamService.findById(teamId);
            if (teamOpt.isPresent()) {
                team = teamOpt.get();
            }
        }

        // Enregistrer le trésor trouvé
        TreasureFound treasureFound = new TreasureFound();
        treasureFound.setTreasure(treasure);
        treasureFound.setUser(user);
        treasureFound.setTeam(team);
        treasureFound.setGameSessionId(gameSessionId);
        treasureFound.setFoundAt(LocalDateTime.now());
        treasureFoundRepository.save(treasureFound);

        // Mettre à jour le score de l'utilisateur
        TreasureHuntScore userScore = getOrCreateScore(treasureHuntScenario.getId(), user, team, gameSessionId);
        userScore.incrementScore(treasure.getPoints());
        treasureHuntScoreRepository.save(userScore);

        // Vérifier si l'utilisateur est maintenant en tête
        boolean isNewLeader = isNewLeaderAfterPoints(treasureHuntScenario.getId(), gameSessionId, user.getId(), userScore.getScore());

        // Notifier tous les participants via WebSocket
        treasureHuntWebSocketHandler.notifyTreasureFound(treasureFound, user.getUsername(),
                team != null ? team.getName() : null, treasure.getPoints(),
                userScore.getScore(), isNewLeader,userId,gameSessionId);

        // Si une équipe est spécifiée, mettre à jour le score de l'équipe
        if (team != null) {
            // Créer un utilisateur fictif pour représenter l'équipe
            User teamUser = new User();
            teamUser.setId(-team.getId()); // ID négatif pour éviter les conflits
            teamUser.setUsername("Team: " + team.getName());

            TreasureHuntScore teamScore = getOrCreateScore(treasureHuntScenario.getId(), teamUser, team, gameSessionId);
            teamScore.incrementScore(treasure.getPoints());
            treasureHuntScoreRepository.save(teamScore);
        }

        // Mettre à jour le tableau des scores
        Map<String, Object> scoreboard = getScoreboard(treasureHuntScenario.getId(), gameSessionId);
        treasureHuntWebSocketHandler.updateScoreboard(treasureHuntScenario.getScenario().getId(), scoreboard);

        return Optional.of(treasureFound);
    }

    @Override
    public boolean isNewLeaderAfterPoints(Long scenarioId, Long gameSessionId, Long userId, int newScore) {
        List<TreasureHuntScore> topScores = treasureHuntScoreRepository
                .findTopScoresByTreasureHuntScenarioIdAndGameSessionId(scenarioId, gameSessionId);

        if (topScores.isEmpty()) return true;

        // Trouver le meilleur score des autres joueurs
        OptionalInt bestOtherScore = topScores.stream()
                .filter(score -> !score.getUser().getId().equals(userId))
                .mapToInt(TreasureHuntScore::getScore)
                .max();

        return !bestOtherScore.isPresent() || newScore > bestOtherScore.getAsInt();
    }

    @Override
    public Map<String, Object> getScoreboardData(Long gameSessionId, Long scenarioId) {
        Optional<TreasureHuntScenario> scenarioOpt = treasureHuntScenarioRepository.findByScenarioId(scenarioId);
        if (scenarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Scénario de chasse au trésor introuvable pour scenarioId: " + scenarioId);
        }

        TreasureHuntScenario scenario = scenarioOpt.get();
        return getScoreboard(scenario.getId(), gameSessionId);
    }


    @Override
    public TreasureHuntScore getOrCreateScore(Long treasureHuntScenarioId, User user, Team team, Long gameSessionId) {
        Optional<TreasureHuntScore> scoreOpt = treasureHuntScoreRepository.findByTreasureHuntScenarioIdAndUserIdAndGameSessionId(
                treasureHuntScenarioId, user.getId(), gameSessionId);

        if (scoreOpt.isPresent()) {
            return scoreOpt.get();
        } else {
            Optional<TreasureHuntScenario> scenarioOpt = treasureHuntScenarioRepository.findById(treasureHuntScenarioId);
            if (!scenarioOpt.isPresent()) {
                throw new IllegalArgumentException("Scénario de chasse au trésor non trouvé: " + treasureHuntScenarioId);
            }

            TreasureHuntScore score = new TreasureHuntScore();
            score.setTreasureHuntScenario(scenarioOpt.get());
            score.setUser(user);
            score.setTeam(team);
            score.setGameSessionId(gameSessionId);
            score.setScore(0);
            score.setTreasuresFound(0);

            return treasureHuntScoreRepository.save(score);
        }
    }

    @Override
    public List<TreasureHuntScore> getScoresByTreasureHuntScenarioIdAndGameSessionId(Long treasureHuntScenarioId, Long gameSessionId) {
        return treasureHuntScoreRepository.findTopScoresByTreasureHuntScenarioIdAndGameSessionId(treasureHuntScenarioId, gameSessionId);
    }

    @Override
    public List<TreasureHuntScore> getScoresByGameSessionId(Long gameSessionId) {
        return treasureHuntScoreRepository.findTopScoresByGameSessionId(gameSessionId);
    }

    @Override
    public Map<String, Object> getScoreboard(Long treasureHuntScenarioId, Long gameSessionId) {
        Map<String, Object> result = new HashMap<>();

        // 🔍 Vérification du scénario
        Optional<TreasureHuntScenario> scenarioOpt = treasureHuntScenarioRepository.findById(treasureHuntScenarioId);
        if (scenarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Scénario de chasse au trésor non trouvé: " + treasureHuntScenarioId);
        }

        TreasureHuntScenario scenario = scenarioOpt.get();

        // 📥 Récupération brute de tous les scores
        List<TreasureHuntScore> allScores = treasureHuntScoreRepository
                .findByTreasureHuntScenarioIdAndGameSessionId(scenario.getId(), gameSessionId);

        // 🧍 Scores individuels
        List<Map<String, Object>> individualScoresList = new ArrayList<>();
        for (TreasureHuntScore score : allScores) {
            if (score.getUser() != null) {
                Map<String, Object> scoreMap = new HashMap<>();
                scoreMap.put("userId", score.getUser().getId());
                scoreMap.put("username", score.getUser().getUsername());
                scoreMap.put("score", score.getScore());
                scoreMap.put("treasuresFound", score.getTreasuresFound());

                if (score.getTeam() != null) {
                    scoreMap.put("teamId", score.getTeam().getId());
                    scoreMap.put("teamName", score.getTeam().getName());
                }

                individualScoresList.add(scoreMap);
            }
        }

        // 👥 Scores d'équipe
        List<Map<String, Object>> teamScoresList = new ArrayList<>();
        for (TreasureHuntScore score : allScores) {
            if (score.getTeam() != null && score.getUser() == null) {
                Map<String, Object> scoreMap = new HashMap<>();
                scoreMap.put("teamId", score.getTeam().getId());
                scoreMap.put("teamName", score.getTeam().getName());
                scoreMap.put("score", score.getScore());
                scoreMap.put("treasuresFound", score.getTreasuresFound());
                teamScoresList.add(scoreMap);
            }
        }

        // 📦 Construction du payload
        result.put("individualScores", individualScoresList);
        result.put("teamScores", teamScoresList);
        result.put("scenarioId", scenario.getId());
        result.put("scenarioName", scenario.getScenario().getName());
        result.put("totalTreasures", scenario.getTotalTreasures());
        result.put("scoresLocked", scenario.getScoresLocked());
        result.put("gameSessionId", gameSessionId);

        return result;
    }


    @Override
    public List<Map<String, Object>> generateQRCodes(Long treasureHuntScenarioId) {
        List<Treasure> treasures = findTreasuresByTreasureHuntId(treasureHuntScenarioId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Treasure treasure : treasures) {
            Map<String, Object> qrCodeData = new HashMap<>();
            qrCodeData.put("treasureId", treasure.getId());
            qrCodeData.put("name", treasure.getName());
            qrCodeData.put("points", treasure.getPoints());
            qrCodeData.put("symbol", treasure.getSymbol());
            qrCodeData.put("qrCode", treasure.getQrCode());

            try {
                byte[] qrCodeImage = generateQRCodeImage(treasure.getQrCode(), 300, 300);
                qrCodeData.put("qrCodeImage", Base64.getEncoder().encodeToString(qrCodeImage));
            } catch (Exception e) {
                logger.error("Erreur lors de la génération de l'image QR code", e);
            }

            result.add(qrCodeData);
        }

        return result;
    }

    @Override
    public String generateQRCodeContent(Treasure treasure) {
        return generateQRCodeContent(
                treasure.getTreasureHuntScenario().getScenario().getName(),
                treasure.getTreasureHuntScenario().getScenario().getId(),
                treasure.getTreasureHuntScenario().getId(),
                treasure.getOrderNumber(),
                treasure.getPoints()
        );
    }

    private String generateQRCodeContent(String scenarioName, Long scenarioId, Long treasureHuntScenarioId, int index, int points) {
        if (scenarioName == null || scenarioName.trim().isEmpty()) {
            scenarioName = "Scenario";
        }
        scenarioName = scenarioName.replaceAll("\\s+", "_"); // Remplacer espaces par underscore
        return "S" + scenarioId + "-TH" + treasureHuntScenarioId + "_TREASURE_" + index + "_PTS_" + points;
    }

    private String generateChecksum(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(content.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().substring(0, 8); // Utiliser seulement les 8 premiers caractères
        } catch (NoSuchAlgorithmException e) {
            logger.error("Erreur lors de la génération du checksum", e);
            return "00000000";
        }
    }

    @Override
    public byte[] generateQRCodeImage(String content, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            return outputStream.toByteArray();
        } catch (WriterException | IOException e) {
            logger.error("Erreur lors de la génération de l'image QR code", e);
            throw new RuntimeException("Erreur lors de la génération de l'image QR code", e);
        }
    }

    @Override
    @Transactional
    public void handleGameStart(Long scenarioId, Long gameSessionId) {
        Optional<TreasureHuntScenario> scenarioOpt = treasureHuntScenarioRepository.findByScenarioId(scenarioId);
        if (scenarioOpt.isPresent()) {
            TreasureHuntScenario scenario = scenarioOpt.get();
            scenario.setActive(true);
            scenario.setScoresLocked(false);
            treasureHuntScenarioRepository.save(scenario);

            // Notifier les clients via WebSocket
            Map<String, Object> scoreboard = getScoreboard(scenario.getId(), gameSessionId);
            treasureHuntWebSocketHandler.updateScoreboard(scenarioId, scoreboard);

            // Envoyer une notification de début de partie
            treasureHuntWebSocketHandler.notifyGameEvent(scenarioId, "GAME_START",
                    "La chasse au trésor commence ! Trouvez les QR codes cachés sur le terrain.");
        }
    }

    @Override
    @Transactional
    public void handleGameEnd(Long scenarioId, Long gameSessionId) {
        Optional<TreasureHuntScenario> scenarioOpt = treasureHuntScenarioRepository.findByScenarioId(scenarioId);
        if (scenarioOpt.isPresent()) {
            TreasureHuntScenario scenario = scenarioOpt.get();
            scenario.setActive(false);
            scenario.setScoresLocked(true);
            treasureHuntScenarioRepository.save(scenario);

            // Notifier les clients via WebSocket
            Map<String, Object> scoreboard = getScoreboard(scenario.getId(), gameSessionId);
            treasureHuntWebSocketHandler.updateScoreboard(scenarioId, scoreboard);

            // Déterminer le gagnant
            List<TreasureHuntScore> scores = treasureHuntScoreRepository
                    .findTopScoresByTreasureHuntScenarioIdAndGameSessionId(scenario.getId(), gameSessionId);

            if (!scores.isEmpty()) {
                TreasureHuntScore winner = scores.get(0);
                String winnerName = winner.getUser().getId() > 0
                        ? winner.getUser().getUsername()
                        : winner.getTeam().getName();

                // Envoyer une notification de fin de partie avec le gagnant
                treasureHuntWebSocketHandler.notifyGameEvent(scenarioId, "GAME_END",
                        "La chasse au trésor est terminée ! " + winnerName +
                                " remporte la victoire avec " + winner.getScore() + " points !");
            } else {
                // Envoyer une notification de fin de partie sans gagnant
                treasureHuntWebSocketHandler.notifyGameEvent(scenarioId, "GAME_END",
                        "La chasse au trésor est terminée !");
            }
        }
    }

    @Override
    public void deleteTreasureHuntScenarioById(Long id) {
        // Vérifier si le scénario de chasse au trésor existe
        Optional<TreasureHuntScenario> treasureHuntScenarioOpt = treasureHuntScenarioRepository.findById(id);
        if (!treasureHuntScenarioOpt.isPresent()) {
            throw new IllegalArgumentException("Scénario de chasse au trésor non trouvé: " + id);
        }
        TreasureHuntScenario treasureHuntScenario = treasureHuntScenarioOpt.get();
        // Supprimer tous les trésors associés
        List<Treasure> treasures = treasureRepository.findByTreasureHuntScenarioId(id);
        for (Treasure treasure : treasures) {
            List<TreasureFound> foundTreasures = treasureFoundRepository.findByTreasureId(treasure.getId());
            treasureFoundRepository.deleteAll(foundTreasures);
        }
        // Supprimer le trésor trouvé
        treasureHuntScenarioRepository.delete(treasureHuntScenario);
    }

    @Override
    @Transactional
    public void deleteTreasure(Treasure treasure) {
        // Vérifier si le trésor existe
        Optional<Treasure> treasureOpt = treasureRepository.findById(treasure.getId());
        if (!treasureOpt.isPresent()) {
            throw new IllegalArgumentException("Trésor non trouvé: " + treasure.getId());
        }
        // Supprimer tous les trésors trouvés associés
        List<TreasureFound> foundTreasures = treasureFoundRepository.findByTreasureId(treasure.getId());
        treasureFoundRepository.deleteAll(foundTreasures);
        // Supprimer le trésor
        treasureRepository.delete(treasure);
    }
}
