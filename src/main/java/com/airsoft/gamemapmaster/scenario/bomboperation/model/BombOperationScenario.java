package com.airsoft.gamemapmaster.scenario.bomboperation.model;

import com.airsoft.gamemapmaster.model.Scenario;
import com.airsoft.gamemapmaster.scenario.bomboperation.dto.BombOperationScenarioDto;
import com.airsoft.gamemapmaster.scenario.bomboperation.dto.BombSiteDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bomb_operation_scenarios")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BombOperationScenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "scenario_id")
    private Scenario scenario;

    @Column(nullable = false)
    private Integer bombTimer = 45; // Temps avant explosion de la bombe

    @Column(nullable = false)
    private Integer defuseTime = 25; // Temps pour désamorcer
    @Column(nullable = false)
    private Integer armingTime = 15; // Temps pour poser la bombe
    @Column(nullable = false)
    private Integer activeSites = 2; // Nombre de sites actifs par round

    @Column(nullable = false)
    private String attackTeamName = "Terroriste"; // Nom par défaut de l'équipe d'attaque

    @Column(nullable = false)
    private String defenseTeamName = "Anti-terroriste"; // Nom par défaut de l'équipe de défense

    @Column(nullable = false)
    private Boolean active = false;

    @Column(nullable = false)
    private Boolean showZones = true; // Afficher les zones sur la carte

    @Column(nullable = false)
    private Boolean showPointsOfInterest = true; // Afficher les points d'intérêt sur la carte

    @JsonIgnore
    @OneToMany(mappedBy = "bombOperationScenario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BombSite> bombSites = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "bombOperationScenario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BombOperationScore> scores = new HashSet<>();

    public BombOperationScenarioDto toDto() {
        BombOperationScenarioDto dto = new BombOperationScenarioDto();
        dto.setId(this.id);
        dto.setScenarioId(this.scenario != null ? this.scenario.getId() : null);
        dto.setBombTimer(this.bombTimer);
        dto.setDefuseTime(this.defuseTime);
        dto.setArmingTime(this.armingTime);
        dto.setActiveSites(this.activeSites);
        dto.setAttackTeamName(this.attackTeamName);
        dto.setDefenseTeamName(this.defenseTeamName);
        dto.setActive(this.active);
        dto.setShowZones(this.showZones);
        dto.setShowPointsOfInterest(this.showPointsOfInterest);

        // ✅ Ajout des BombSites
        if (this.bombSites != null && !this.bombSites.isEmpty()) {
            List<BombSiteDto> siteDtos = this.bombSites.stream()
                    .map(site -> {
                        BombSiteDto siteDto = new BombSiteDto();
                        siteDto.setId(site.getId());
                        siteDto.setName(site.getName());
                        siteDto.setLatitude(site.getLatitude());
                        siteDto.setLongitude(site.getLongitude());
                        siteDto.setRadius(site.getRadius());
                        siteDto.setBombOperationScenarioId(this.id);
                        siteDto.setScenarioId(this.scenario != null ? this.scenario.getId() : null);
                        return siteDto;
                    })
                    .collect(Collectors.toList());
            dto.setBombSites(siteDtos);
        }

        // ✅ Ajout des Scores
/*        if (this.scores != null && !this.scores.isEmpty()) {
            List<BombOperationScoreDto> scoreDtos = this.scores.stream()
                    .map(score -> {
                        BombOperationScoreDto scoreDto = new BombOperationScoreDto();
                        scoreDto.setId(score.getId());
                        scoreDto.setTeamId(score.getTeamId());
                        scoreDto.setPoints(score.getPoints());
                        scoreDto.setGameSessionId(score.getGameSession() != null ? score.getGameSession().getId() : null);
                        return scoreDto;
                    })
                    .collect(Collectors.toList());
            dto.setScores(scoreDtos);
        }*/

        return dto;
    }

}
