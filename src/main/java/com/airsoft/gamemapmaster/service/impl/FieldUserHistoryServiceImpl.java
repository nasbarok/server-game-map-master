package com.airsoft.gamemapmaster.service.impl;

import com.airsoft.gamemapmaster.model.Field;
import com.airsoft.gamemapmaster.model.FieldUserHistory;
import com.airsoft.gamemapmaster.model.User;
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
        List<Field> fields = fieldUserHistoryRepository.findFieldsVisitedByUser(userId);
        return fields;
    }

    @Override
    public boolean deleteHistoryEntryIfOwnedByUser(Long historyId, User user) {
        List<FieldUserHistory> historyListOpt = fieldUserHistoryRepository.findAllFieldUserHistoriesByUserIdAndFieldId(user.getId(),historyId);

        for (FieldUserHistory history : historyListOpt) {
            if (history.getUser().getId().equals(user.getId())) {
                fieldUserHistoryRepository.deleteById(history.getId());
            }
            return true;
        }

        return false;
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
