package com.airsoft.gamemapmaster.model.DTO;

public class GeocodingResultDto {
    private String displayName;
    private double latitude;
    private double longitude;

    public GeocodingResultDto() {
    }

    public GeocodingResultDto(String displayName, double latitude, double longitude) {
        this.displayName = displayName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and Setters
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}

