package com.airsoft.gamemapmaster.service.impl;

import com.airsoft.gamemapmaster.model.Field;
import com.airsoft.gamemapmaster.model.FieldUserHistory;
import com.airsoft.gamemapmaster.repository.FieldRepository;
import com.airsoft.gamemapmaster.repository.FieldUserHistoryRepository;
import com.airsoft.gamemapmaster.service.FieldService;
import com.airsoft.gamemapmaster.service.FieldUserHistoryService;
import com.airsoft.gamemapmaster.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FieldUserHistoryServiceImpl implements FieldUserHistoryService {

    @Autowired
    private FieldUserHistoryRepository fieldUserHistoryRepository;

    @Autowired
    private FieldRepository fieldRepository;
    @Autowired
    private UserService userService;

    @Autowired
    private FieldService fieldService;

    @Override
    public FieldUserHistory logJoin(Long userId, Long fieldId) {
        FieldUserHistory history = new FieldUserHistory();
        history.setUser(userService.findById(userId).orElseThrow());
        history.setField(fieldService.findById(fieldId).orElseThrow());
        history.setJoinedAt(LocalDateTime.now());
        history.setSessionClosed(false);
        return fieldUserHistoryRepository.save(history);
    }

    @Override
    public void logLeave(Long userId, Long fieldId) {
        Optional<FieldUserHistory> historyOpt = fieldUserHistoryRepository.findLatestByUserIdAndFieldId(userId, fieldId);
        if (historyOpt.isPresent()) {
            FieldUserHistory history = historyOpt.get();
            history.setLeftAt(LocalDateTime.now());
            history.setSessionClosed(true);
            history.setLeftAt(LocalDateTime.now());
            fieldUserHistoryRepository.save(history);
        }
    }

    @Override
    public void closeSessionsForField(Long fieldId) {
        List<FieldUserHistory> sessions = fieldUserHistoryRepository.findByFieldId(fieldId);
        for (FieldUserHistory history : sessions) {
            if (!history.isSessionClosed()) {
                history.setLeftAt(LocalDateTime.now());
                history.setSessionClosed(true);
                fieldUserHistoryRepository.save(history);
            }
        }
    }

    @Override
    public List<Field> getFieldsVisitedByUser(Long userId) {
        // Récupérer les IDs des terrains visités par l'utilisateur
        List<Long> fieldIds = fieldUserHistoryRepository.findFieldIdsByUserId(userId);

        // Récupérer les terrains correspondants
        List<Field> fields = new ArrayList<>();
        for (Long fieldId : fieldIds) {
            Optional<Field> fieldOpt = fieldRepository.findById(fieldId);
            fieldOpt.ifPresent(fields::add);
        }

        return fields;
    }

    @Override
    public List<FieldUserHistory> getHistoryForUser(Long userId) {
        return fieldUserHistoryRepository.findByUserIdAndSessionClosedTrue(userId);
    }

    @Override
    public List<FieldUserHistory> getHistoryForField(Long fieldId) {
        return fieldUserHistoryRepository.findByFieldId(fieldId);
    }
}
