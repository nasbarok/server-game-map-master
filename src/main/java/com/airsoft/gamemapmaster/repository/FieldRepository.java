package com.airsoft.gamemapmaster.repository;

import com.airsoft.gamemapmaster.model.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FieldRepository extends JpaRepository<Field, Long> {
    List<Field> findByOwnerId(Long ownerId);
}
