package com.airsoft.gamemapmaster.repository;

import com.airsoft.gamemapmaster.model.GameSession;
import com.airsoft.gamemapmaster.model.Invitation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    Optional<GameSession> findFirstByFieldIdAndActiveTrue(Long fieldId);

    List<GameSession> findByGameMapId(Long gameMapId);
    List<GameSession> findByActiveTrue();
    Optional<GameSession> findByIdAndActiveTrue(Long id);

    Optional<GameSession> findFirstByFieldIdAndActiveTrueOrderByStartTimeDesc(Long fieldId);

    List<GameSession> findByFieldId(Long fieldId);

    List<GameSession> findByParticipantsUserId(Long userId);

    // ✅ Pagination + filtres date par terrain
    @Query("select gs from GameSession gs where gs.field.id = :fieldId and (:start is null or gs.startTime >= :start) and (:end   is null or gs.startTime <= :end) order by gs.startTime desc")
    Page<GameSession> findByFieldIdWithDateFilter(@Param("fieldId") Long fieldId,
                                                  @Param("start") OffsetDateTime start,
                                                  @Param("end") OffsetDateTime end,
                                                  Pageable pageable);

    // ✅ Pagination + filtres date par utilisateur (via la relation participants -> user)
    @Query("select gs from GameSession gs join gs.participants p where p.user.id = :userId and (:start is null or gs.startTime >= :start) and (:end   is null or gs.startTime <= :end) order by gs.startTime desc")
    Page<GameSession> findByUserIdWithDateFilter(@Param("userId") Long userId,
                                                 @Param("start") OffsetDateTime start,
                                                 @Param("end") OffsetDateTime end,
                                                 Pageable pageable);
}
