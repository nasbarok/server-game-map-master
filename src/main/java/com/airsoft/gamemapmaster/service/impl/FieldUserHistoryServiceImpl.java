package com.airsoft.gamemapmaster.service.impl;

import com.airsoft.gamemapmaster.model.FieldUserHistory;
import com.airsoft.gamemapmaster.repository.FieldUserHistoryRepository;
import com.airsoft.gamemapmaster.service.FieldService;
import com.airsoft.gamemapmaster.service.FieldUserHistoryService;
import com.airsoft.gamemapmaster.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FieldUserHistoryServiceImpl implements FieldUserHistoryService {

    @Autowired
    private FieldUserHistoryRepository historyRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private FieldService fieldService;

    @Override
    public FieldUserHistory logJoin(Long userId, Long fieldId) {
        Optional<FieldUserHistory> existing = historyRepository.findByUserIdAndFieldIdAndSessionClosedFalse(userId, fieldId);
        if (existing.isPresent()) return existing.get();

        FieldUserHistory history = new FieldUserHistory();
        history.setUser(userService.findById(userId).orElseThrow());
        history.setField(fieldService.findById(fieldId).orElseThrow());
        history.setJoinedAt(LocalDateTime.now());
        history.setSessionClosed(false);
        return historyRepository.save(history);
    }

    @Override
    public void logLeave(Long userId, Long fieldId) {
        Optional<FieldUserHistory> optional = historyRepository.findByUserIdAndFieldIdAndSessionClosedFalse(userId, fieldId);
        optional.ifPresent(history -> {
            history.setLeftAt(LocalDateTime.now());
            history.setSessionClosed(true);
            historyRepository.save(history);
        });
    }

    @Override
    public void closeSessionsForField(Long fieldId) {
        List<FieldUserHistory> sessions = historyRepository.findByFieldId(fieldId);
        for (FieldUserHistory history : sessions) {
            if (!history.isSessionClosed()) {
                history.setLeftAt(LocalDateTime.now());
                history.setSessionClosed(true);
                historyRepository.save(history);
            }
        }
    }

    @Override
    public List<FieldUserHistory> getHistoryForUser(Long userId) {
        return historyRepository.findByUserIdAndSessionClosedTrue(userId);
    }

    @Override
    public List<FieldUserHistory> getHistoryForField(Long fieldId) {
        return historyRepository.findByFieldId(fieldId);
    }
}
