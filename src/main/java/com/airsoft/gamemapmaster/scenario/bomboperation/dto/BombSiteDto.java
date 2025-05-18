package com.airsoft.gamemapmaster.scenario.bomboperation.dto;

public class BombSiteDto {
    private Long id;
    private String name;
    private Double latitude;
    private Double longitude;
    private Double radius;
    private Long bombOperationScenarioId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getRadius() {
        return radius;
    }

    public void setRadius(Double radius) {
        this.radius = radius;
    }

    public Long getBombOperationScenarioId() {
        return bombOperationScenarioId;
    }

    public void setBombOperationScenarioId(Long bombOperationScenarioId) {
        this.bombOperationScenarioId = bombOperationScenarioId;
    }
}
