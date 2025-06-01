package com.airsoft.gamemapmaster.scenario.bomboperation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BombOperationActionDTO {
    private String type;
    private Long senderId;
    private Long gameSessionId;
    private String action;
    private Map<String, Integer> payload;
}