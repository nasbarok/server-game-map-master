package com.airsoft.gamemapmaster.model.DTO;

import com.airsoft.gamemapmaster.model.GameMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameMapDTO {

    private static final Logger logger = LoggerFactory.getLogger(GameMapDTO.class);

    private Long id;
    private String name;
    private String description;
    private String sourceAddress;
    private Double centerLatitude;
    private Double centerLongitude;
    private Double initialZoom;

    private String fieldBoundaryJson;
    private String mapZonesJson;
    private String mapPointsOfInterestJson;

    private String backgroundImageBase64;
    private String backgroundBoundsJson;

    private String satelliteImageBase64;
    private String satelliteBoundsJson;

    public static GameMapDTO fromEntity(GameMap entity) {
        if (entity == null) {
            logger.warn("⚠️ GameMap entity est null !");
            return null;
        }

        logger.info("🗺️ [GameMapDTO] Mapping GameMap entity → DTO : ID={}, name={}", entity.getId(), entity.getName());
        logger.info("🖼️ [GameMapDTO] backgroundImageBase64: {} caractères",
                entity.getBackgroundImageBase64() != null ? entity.getBackgroundImageBase64().length() : 0);
        logger.info("🛰️ [GameMapDTO] satelliteImageBase64: {} caractères",
                entity.getSatelliteImageBase64() != null ? entity.getSatelliteImageBase64().length() : 0);
        logger.info("📐 [GameMapDTO] backgroundBoundsJson present: {}", entity.getBackgroundBoundsJson() != null);
        logger.info("📡 [GameMapDTO] satelliteBoundsJson present: {}", entity.getSatelliteBoundsJson() != null);
        logger.info("📏 [GameMapDTO] fieldBoundaryJson present: {}", entity.getFieldBoundaryJson() != null);
        logger.info("🧭 [GameMapDTO] mapZonesJson present: {}", entity.getMapZonesJson() != null);
        logger.info("📍 [GameMapDTO] mapPointsOfInterestJson present: {}", entity.getMapPointsOfInterestJson() != null);

        return new GameMapDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getSourceAddress(),
                entity.getCenterLatitude(),
                entity.getCenterLongitude(),
                entity.getInitialZoom(),
                entity.getFieldBoundaryJson(),
                entity.getMapZonesJson(),
                entity.getMapPointsOfInterestJson(),
                entity.getBackgroundImageBase64(),
                entity.getBackgroundBoundsJson(),
                entity.getSatelliteImageBase64(),
                entity.getSatelliteBoundsJson()
        );
    }
}
