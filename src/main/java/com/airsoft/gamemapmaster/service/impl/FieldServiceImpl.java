package com.airsoft.gamemapmaster.service.impl;

import com.airsoft.gamemapmaster.model.Field;
import com.airsoft.gamemapmaster.repository.FieldRepository;
import com.airsoft.gamemapmaster.service.FieldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FieldServiceImpl implements FieldService {

    @Autowired
    private FieldRepository fieldRepository;

    @Override
    public List<Field> findAll() {
        return fieldRepository.findAll();
    }

    @Override
    public Optional<Field> findById(Long id) {
        return fieldRepository.findById(id);
    }

    @Override
    public List<Field> findByOwnerId(Long ownerId) {
        return fieldRepository.findByOwnerId(ownerId);
    }

    @Override
    public Field save(Field field) {
        return fieldRepository.save(field);
    }

    @Override
    public void deleteById(Long id) {
        fieldRepository.deleteById(id);
    }
}
