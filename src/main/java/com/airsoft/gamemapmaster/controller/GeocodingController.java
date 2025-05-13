package com.airsoft.gamemapmaster.controller;

import com.airsoft.gamemapmaster.model.DTO.GeocodingResultDto;
import com.airsoft.gamemapmaster.service.GeocodingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/geocoding")
public class GeocodingController {

    private final GeocodingService geocodingService;

    @Autowired
    public GeocodingController(GeocodingService geocodingService) {
        this.geocodingService = geocodingService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<GeocodingResultDto>> searchAddress(@RequestParam String address) {
        List<GeocodingResultDto> results = geocodingService.searchAddress(address);
        if (results.isEmpty()) {
            // Vous pourriez vouloir retourner un statut différent si aucun résultat n'est trouvé,
            // par exemple, Not Found (404) ou No Content (204), selon votre convention API.
            // Pour l'instant, nous retournons OK (200) avec une liste vide.
            return ResponseEntity.ok(results);
        }
        return ResponseEntity.ok(results);
    }
}

