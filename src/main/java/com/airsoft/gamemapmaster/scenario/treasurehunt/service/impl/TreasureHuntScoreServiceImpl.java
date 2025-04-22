package com.airsoft.gamemapmaster.scenario.treasurehunt.service.impl;

import com.airsoft.gamemapmaster.model.Team;
import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.scenario.treasurehunt.model.TreasureHuntScenario;
import com.airsoft.gamemapmaster.scenario.treasurehunt.model.TreasureHuntScore;
import com.airsoft.gamemapmaster.scenario.treasurehunt.repository.TreasureHuntScenarioRepository;
import com.airsoft.gamemapmaster.scenario.treasurehunt.repository.TreasureHuntScoreRepository;
import com.airsoft.gamemapmaster.scenario.treasurehunt.service.TreasureHuntScoreService;
import com.airsoft.gamemapmaster.service.TeamService;
import com.airsoft.gamemapmaster.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TreasureHuntScoreServiceImpl implements TreasureHuntScoreService {

    @Autowired
    private TreasureHuntScoreRepository scoreRepository;

    @Autowired
    private TreasureHuntScenarioRepository scenarioRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TeamService teamService;

    @Override
    @Transactional
    public TreasureHuntScore updateScore(Long treasureHuntScenarioId, Long userId, Integer pointsToAdd) {
        // Vérifier si le scénario existe et si les scores sont verrouillés
        TreasureHuntScenario scenario = scenarioRepository.findById(treasureHuntScenarioId)
                .orElseThrow(() -> new RuntimeException("Treasure hunt scenario not found with id: " + treasureHuntScenarioId));

        if (scenario.getScoresLocked()) {
            throw new RuntimeException("Scores are locked for this scenario");
        }

        // Récupérer l'utilisateur
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Récupérer ou créer le score
        TreasureHuntScore score = scoreRepository.findByTreasureHuntScenarioIdAndUserId(treasureHuntScenarioId, userId)
                .orElseGet(() -> {
                    TreasureHuntScore newScore = new TreasureHuntScore();
                    newScore.setTreasureHuntScenario(scenario);
                    newScore.setUser(user);
                    newScore.setScore(0);
                    newScore.setTreasuresFound(0);

                    // Si l'utilisateur a une équipe, l'associer au score
                    teamService.findTeamByUserId(userId).ifPresent(newScore::setTeam);

                    return newScore;
                });

        // Mettre à jour le score
        score.incrementScore(pointsToAdd);

        return scoreRepository.save(score);
    }

    @Override
    @Transactional
    public TreasureHuntScore getOrCreateScore(Long treasureHuntScenarioId, Long userId, Long teamId) {
        // Vérifier si le scénario existe
        TreasureHuntScenario scenario = scenarioRepository.findById(treasureHuntScenarioId)
                .orElseThrow(() -> new RuntimeException("Treasure hunt scenario not found with id: " + treasureHuntScenarioId));

        // Récupérer l'utilisateur
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Récupérer l'équipe si fournie
        Team team;
        if (teamId != null) {
            team = teamService.findById(teamId)
                    .orElseThrow(() -> new RuntimeException("Team not found with id: " + teamId));
        } else {
            team = null;
        }

        // Récupérer ou créer le score
        return scoreRepository.findByTreasureHuntScenarioIdAndUserId(treasureHuntScenarioId, userId)
                .orElseGet(() -> {
                    TreasureHuntScore newScore = new TreasureHuntScore();
                    newScore.setTreasureHuntScenario(scenario);
                    newScore.setUser(user);
                    newScore.setTeam(team);
                    newScore.setScore(0);
                    newScore.setTreasuresFound(0);
                    return scoreRepository.save(newScore);
                });
    }

    @Override
    public Optional<TreasureHuntScore> findScore(Long treasureHuntScenarioId, Long userId) {
        return scoreRepository.findByTreasureHuntScenarioIdAndUserId(treasureHuntScenarioId, userId);
    }

    @Override
    public List<TreasureHuntScore> getIndividualScores(Long treasureHuntScenarioId) {
        return scoreRepository.findByTreasureHuntScenarioIdOrderByScoreDesc(treasureHuntScenarioId);
    }

    @Override
    public List<TreasureHuntScore> getTeamScores(Long treasureHuntScenarioId) {
        // Récupérer tous les scores individuels
        List<TreasureHuntScore> individualScores = scoreRepository.findByTreasureHuntScenarioId(treasureHuntScenarioId);

        // Regrouper par équipe et calculer les totaux
        Map<Team, TeamScoreAggregate> teamScores = new HashMap<>();

        for (TreasureHuntScore score : individualScores) {
            if (score.getTeam() != null) {
                TeamScoreAggregate aggregate = teamScores.computeIfAbsent(score.getTeam(), team -> new TeamScoreAggregate());
                aggregate.addScore(score.getScore());
                aggregate.addTreasuresFound(score.getTreasuresFound());
            }
        }

        // Convertir en liste de TreasureHuntScore pour les équipes
        List<TreasureHuntScore> result = new ArrayList<>();
        TreasureHuntScenario scenario = scenarioRepository.findById(treasureHuntScenarioId)
                .orElseThrow(() -> new RuntimeException("Treasure hunt scenario not found with id: " + treasureHuntScenarioId));

        for (Map.Entry<Team, TeamScoreAggregate> entry : teamScores.entrySet()) {
            TreasureHuntScore teamScore = new TreasureHuntScore();
            teamScore.setTreasureHuntScenario(scenario);
            teamScore.setTeam(entry.getKey());
            teamScore.setScore(entry.getValue().getTotalScore());
            teamScore.setTreasuresFound(entry.getValue().getTotalTreasuresFound());
            result.add(teamScore);
        }

        // Trier par score décroissant
        result.sort((a, b) -> b.getScore().compareTo(a.getScore()));

        return result;
    }

    @Override
    public Map<String, Object> getScoreboard(Long treasureHuntScenarioId) {
        Map<String, Object> scoreboard = new HashMap<>();

        // Récupérer le scénario
        TreasureHuntScenario scenario = scenarioRepository.findById(treasureHuntScenarioId)
                .orElseThrow(() -> new RuntimeException("Treasure hunt scenario not found with id: " + treasureHuntScenarioId));

        // Récupérer les scores individuels et par équipe
        List<TreasureHuntScore> individualScores = getIndividualScores(treasureHuntScenarioId);
        List<TreasureHuntScore> teamScores = getTeamScores(treasureHuntScenarioId);

        // Convertir les scores en format approprié pour le client
        List<Map<String, Object>> individualScoresMaps = individualScores.stream()
                .map(this::convertScoreToMap)
                .collect(Collectors.toList());

        List<Map<String, Object>> teamScoresMaps = teamScores.stream()
                .map(this::convertScoreToMap)
                .collect(Collectors.toList());

        // Construire le scoreboard
        scoreboard.put("individualScores", individualScoresMaps);
        scoreboard.put("teamScores", teamScoresMaps);
        scoreboard.put("scoresLocked", scenario.getScoresLocked());

        return scoreboard;
    }

    @Override
    @Transactional
    public void lockScores(Long treasureHuntScenarioId, Boolean locked) {
        TreasureHuntScenario scenario = scenarioRepository.findById(treasureHuntScenarioId)
                .orElseThrow(() -> new RuntimeException("Treasure hunt scenario not found with id: " + treasureHuntScenarioId));

        scenario.setScoresLocked(locked);
        scenarioRepository.save(scenario);
    }

    @Override
    @Transactional
    public void resetScores(Long treasureHuntScenarioId) {
        // Vérifier si le scénario existe
        if (!scenarioRepository.existsById(treasureHuntScenarioId)) {
            throw new RuntimeException("Treasure hunt scenario not found with id: " + treasureHuntScenarioId);
        }

        // Supprimer tous les scores pour ce scénario
        scoreRepository.deleteByTreasureHuntScenarioId(treasureHuntScenarioId);
    }

    @Override
    public boolean areScoresLocked(Long treasureHuntScenarioId) {
        return scenarioRepository.findById(treasureHuntScenarioId)
                .map(TreasureHuntScenario::getScoresLocked)
                .orElse(false);
    }

    // Méthode utilitaire pour convertir un score en Map
    private Map<String, Object> convertScoreToMap(TreasureHuntScore score) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", score.getId());

        if (score.getUser() != null) {
            map.put("userId", score.getUser().getId());
            map.put("username", score.getUser().getUsername());
        }

        if (score.getTeam() != null) {
            map.put("teamId", score.getTeam().getId());
            map.put("teamName", score.getTeam().getName());
        }

        map.put("score", score.getScore());
        map.put("treasuresFound", score.getTreasuresFound());

        return map;
    }

    // Classe utilitaire pour agréger les scores par équipe
    private static class TeamScoreAggregate {
        private int totalScore = 0;
        private int totalTreasuresFound = 0;

        public void addScore(int score) {
            totalScore += score;
        }

        public void addTreasuresFound(int treasuresFound) {
            totalTreasuresFound += treasuresFound;
        }

        public int getTotalScore() {
            return totalScore;
        }

        public int getTotalTreasuresFound() {
            return totalTreasuresFound;
        }
    }
}
