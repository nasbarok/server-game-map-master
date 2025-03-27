package com.airsoft.gamemapmaster.service;

import com.airsoft.gamemapmaster.model.FieldUserHistory;

import java.util.List;

public interface FieldUserHistoryService {

    FieldUserHistory logJoin(Long userId, Long fieldId);

    void logLeave(Long userId, Long fieldId);

    void closeSessionsForField(Long fieldId);

    List<FieldUserHistory> getHistoryForUser(Long userId);

    List<FieldUserHistory> getHistoryForField(Long fieldId);
}
