package com.airsoft.gamemapmaster.repository;

import com.airsoft.gamemapmaster.model.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FieldRepository extends JpaRepository<Field, Long> {
    List<Field> findByOwnerId(Long ownerId);

    List<Field> findByOwnerIdAndActiveTrue(Long id);

    @Query("SELECT f FROM Field f WHERE f.owner.id = :ownerId AND f.active = true AND f.closedAt IS NULL ORDER BY f.id DESC")
    List<Field> findLastOpenedFieldByOwner(@Param("ownerId") Long ownerId);
}
