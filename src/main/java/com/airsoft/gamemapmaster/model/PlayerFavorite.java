package com.airsoft.gamemapmaster.model;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "player_favorites")
@Data
public class PlayerFavorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;  // L'host qui marque en favori

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "favorite_player_id", nullable = false)
    private User favoritePlayer;  // Le joueur marqué en favori

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    // Constructeurs
    public PlayerFavorite() {
        this.createdAt = OffsetDateTime.now();
    }

    public PlayerFavorite(User host, User favoritePlayer) {
        this();
        this.host = host;
        this.favoritePlayer = favoritePlayer;
    }
}