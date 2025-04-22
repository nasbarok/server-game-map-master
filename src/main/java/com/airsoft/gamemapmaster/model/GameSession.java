package com.airsoft.gamemapmaster.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "game_sessions")
public class GameSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Field field;

    @ManyToOne
    @JoinColumn(name = "game_map_id")
    private GameMap gameMap;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;
    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false)
    private Boolean active = false;

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameSessionParticipant> participants;

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameSessionScenario> scenarios;

    @PrePersist
    protected void onCreate() {
        if (startTime == null) {
            startTime = LocalDateTime.now();
        }
        if (active == null) {
            active = false;
        }
    }

    public boolean isExpired() {
        if (!active || endTime != null) {
            return true;
        }

        LocalDateTime expirationTime = startTime.plusMinutes(durationMinutes);
        return LocalDateTime.now().isAfter(expirationTime);
    }

    public long getRemainingTimeInSeconds() {
        if (!active || endTime != null) {
            return 0;
        }

        LocalDateTime expirationTime = startTime.plusMinutes(durationMinutes);
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(expirationTime)) {
            return 0;
        }

        return java.time.Duration.between(now, expirationTime).getSeconds();
    }
}
