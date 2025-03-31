package com.airsoft.gamemapmaster.repository;

import com.airsoft.gamemapmaster.model.Field;
import com.airsoft.gamemapmaster.model.FieldUserHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
    List<Field> findFieldsVisitedByUser(Long id);

    Optional<FieldUserHistory> findLatestByUserIdAndFieldId(Long userId, Long fieldId);

    List<Long> findFieldIdsByUserId(Long userId);
}
