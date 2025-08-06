package com.airsoft.gamemapmaster.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
    private OffsetDateTime startTime;

    private OffsetDateTime endTime;
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
            startTime = OffsetDateTime.now(ZoneOffset.UTC);
        }
        if (active == null) {
            active = false;
        }
    }

    public boolean isExpired() {
        if (!active || endTime != null) {
            return true;
        }

        OffsetDateTime expirationTime = startTime.plusMinutes(durationMinutes);
        return OffsetDateTime.now(ZoneOffset.UTC).isAfter(expirationTime);
    }

    public long getRemainingTimeInSeconds() {
        if (!active || endTime != null) {
            return 0;
        }

        OffsetDateTime expirationTime = startTime.plusMinutes(durationMinutes);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        if (now.isAfter(expirationTime)) {
            return 0;
        }

        return java.time.Duration.between(now, expirationTime).getSeconds();
    }
}
