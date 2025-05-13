package com.airsoft.gamemapmaster.service.impl;

import com.airsoft.gamemapmaster.model.DTO.GeocodingResultDto;
import com.airsoft.gamemapmaster.security.controller.AuthController;
import com.airsoft.gamemapmaster.service.GeocodingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
public class GeocodingServiceImpl implements GeocodingService {
    private static final Logger logger = LoggerFactory.getLogger(GeocodingServiceImpl.class);
    private static final String NOMINATIM_USER_AGENT = "AirsoftGameMapMasterApp/1.0 (https://github.com/nasbarok/server-game-map-master) ";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeocodingServiceImpl() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<GeocodingResultDto> searchAddress(String address) {
        logger.info("🔍 [Geocoding]  Recherche de l'adresse : {}", address);
        String url = "https://nominatim.openstreetmap.org/search";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("q", address)
                .queryParam("format", "json")
                .queryParam("limit", 5); // Limiter le nombre de résultats

        List<GeocodingResultDto> results = new ArrayList<>();

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", NOMINATIM_USER_AGENT);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            logger.info("[Geocoding] URL appelée : " + builder.toUriString());
            URI uri = builder.encode().build().toUri();
            ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
            String responseBody = responseEntity.getBody();
            logger.info("[Geocoding] Réponse reçue : " + responseBody);

            JsonNode root = objectMapper.readTree(responseBody);
            if (root.isArray()) {
                for (JsonNode node : root) {

                    String displayName = node.path("display_name").asText(null);
                    double latitude = node.path("lat").asDouble(0.0);
                    double longitude = node.path("lon").asDouble(0.0);
                    if (displayName != null && latitude != 0.0 && longitude != 0.0) {
                        logger.info("[Geocoding] Résultat : {} (lat: {}, lon: {})", displayName, latitude, longitude);
                        results.add(new GeocodingResultDto(displayName, latitude, longitude));
                    }
                }
            }
        } catch (Exception e) {
            // Gérer les erreurs, par exemple, logger l'exception
            // Pour l'instant, nous retournons une liste vide en cas d'erreur
            logger.error("Error during geocoding request: " + e.getMessage());
        }
        return results;
    }
}

