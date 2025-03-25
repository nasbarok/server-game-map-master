package com.airsoft.gamemapmaster.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fields")
public class Field {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    private String address;
    
    private Double latitude;
    
    private Double longitude;
    
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
    
    private Double sizeX; // Taille en mètres (axe X)
    
    private Double sizeY; // Taille en mètres (axe Y)
    
    private String imageUrl; // URL de l'image du terrain

}
