package com.airsoft.gamemapmaster.service;

import com.airsoft.gamemapmaster.model.DTO.GeocodingResultDto;

import java.util.List;

public interface GeocodingService {
    List<GeocodingResultDto> searchAddress(String address);
}

