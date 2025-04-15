package com.airsoft.gamemapmaster.event;

import org.springframework.context.ApplicationEvent;

public class GameStartEvent extends ApplicationEvent {
    private final Long gameId;
    private final Long gameSessionId;

    public GameStartEvent(Object source, Long gameId, Long gameSessionId) {
        super(source);
        this.gameId = gameId;
        this.gameSessionId = gameSessionId;
    }

    public Long getGameId() {
        return gameId;
    }

    public Long getGameSessionId() {
        return gameSessionId;
    }
}
