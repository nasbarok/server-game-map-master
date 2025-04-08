package com.airsoft.gamemapmaster.service;

import com.airsoft.gamemapmaster.model.Field;
import com.airsoft.gamemapmaster.model.FieldUserHistory;
import com.airsoft.gamemapmaster.model.User;

import java.util.List;

public interface FieldUserHistoryService {

    FieldUserHistory logJoin(Long userId, Long fieldId);

    void logLeave(Long userId, Long fieldId);

    void closeSessionsForField(Long fieldId);

    List<FieldUserHistory> getHistoryForUser(Long userId);

    List<FieldUserHistory> getHistoryForField(Long fieldId);

    List<Field> getFieldsVisitedByUser(Long id);

    boolean deleteHistoryEntryIfOwnedByUser(Long historyId, User user);
}
