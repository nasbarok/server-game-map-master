package com.airsoft.gamemapmaster.scenario.bomboperation.model;


import com.airsoft.gamemapmaster.scenario.bomboperation.dto.BombSiteDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bomb_sites")
public class BombSite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // "A", "B", "C", etc.

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Double radius = 10.0; // Rayon en mètres

    @ManyToOne
    @JoinColumn(name = "bomb_operation_scenario_id", nullable = false)
    private BombOperationScenario bombOperationScenario;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = false;

    private Long scenarioId; // ID du scénario de base, pour faciliter les requêtes

    public BombSiteDto toDto() {
        BombSiteDto dto = new BombSiteDto();
        dto.setId(this.id);
        dto.setName(this.name);
        dto.setLatitude(this.latitude);
        dto.setLongitude(this.longitude);
        dto.setRadius(this.radius);
        dto.setBombOperationScenarioId(this.bombOperationScenario != null ? this.bombOperationScenario.getId() : null);
        dto.setScenarioId(this.scenarioId);
        return dto;
    }
}
