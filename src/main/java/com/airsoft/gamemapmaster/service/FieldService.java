package com.airsoft.gamemapmaster.service;

import com.airsoft.gamemapmaster.model.Field;

import java.util.List;
import java.util.Optional;

public interface FieldService {
    List<Field> findAll();
    Optional<Field> findById(Long id);
    List<Field> findByOwnerId(Long ownerId);
    Field save(Field field);
    void deleteById(Long id);
}
