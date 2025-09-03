package com.airsoft.gamemapmaster.repository;

import com.airsoft.gamemapmaster.model.DTO.FieldDTO;
import com.airsoft.gamemapmaster.model.Field;
import com.airsoft.gamemapmaster.model.FieldUserHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FieldUserHistoryRepository extends JpaRepository<FieldUserHistory, Long> {

    List<FieldUserHistory> findByUserIdAndSessionClosedTrue(Long userId);

    List<FieldUserHistory> findByFieldId(Long fieldId);

    Optional<FieldUserHistory> findByUserIdAndFieldIdAndSessionClosedFalse(Long userId, Long fieldId);

    List<FieldUserHistory> findByUserId(Long userId);

    Optional<FieldUserHistory> findTopByUserIdAndSessionClosedFalseOrderByFieldIdDesc(Long id);

    @Query("SELECT h.field FROM FieldUserHistory h WHERE h.user.id = :id")
    List<Field> findFieldsVisitedByUser(@Param("id") Long id);

    Optional<FieldUserHistory> findLatestByUserIdAndFieldId(Long userId, Long fieldId);

    @Query("SELECT h FROM FieldUserHistory h WHERE h.user.id = :userId AND h.field.id = :fieldId")
    List<FieldUserHistory> findAllFieldUserHistoriesByUserIdAndFieldId(
            @Param("userId") Long userId,
            @Param("fieldId") Long fieldId
    );
    @Query("SELECT f FROM Field f " +
            "INNER JOIN FieldUserHistory fuh ON f.id = fuh.field.id " +
            "WHERE fuh.user.id = :userId AND f.active = true " +
            "ORDER BY fuh.joinedAt DESC")
    List<Field> findActiveFieldsByUserId(@Param("userId") Long userId);

    @Query("SELECT f FROM Field f JOIN FieldUserHistory h ON f.id = h.field.id WHERE h.user.id = :userId")
    Page<Field> findFieldsVisitedByUserId(@Param("userId") Long userId, Pageable pageable);
}
