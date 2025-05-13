package com.airsoft.gamemapmaster.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "game_maps")
public class GameMap {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "field_id")
    private Field field;
    
    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;
    
    @Column(nullable = false)
    private boolean active = true;
    
    @OneToMany(mappedBy = "gameMap", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Scenario> scenarios = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    // New fields for interactive map features
    @Column(name = "source_address")
    private String sourceAddress;

    @Column(name = "center_latitude")
    private Double centerLatitude;

    @Column(name = "center_longitude")
    private Double centerLongitude;

    @Column(name = "initial_zoom")
    private Double initialZoom;

    @Lob
    @Column(name = "field_boundary_json", columnDefinition = "TEXT")
    private String fieldBoundaryJson;

    @Lob
    @Column(name = "map_zones_json", columnDefinition = "TEXT")
    private String mapZonesJson;

    @Lob
    @Column(name = "map_points_of_interest_json", columnDefinition = "TEXT")
    private String mapPointsOfInterestJson;

    @Lob
    @Column(name = "background_image_base64", columnDefinition = "LONGTEXT") // Using LONGTEXT for potentially large Base64 strings
    private String backgroundImageBase64;
}
