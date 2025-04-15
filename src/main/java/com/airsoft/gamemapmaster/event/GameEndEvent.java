package com.airsoft.gamemapmaster.event;

import org.springframework.context.ApplicationEvent;

public class GameEndEvent extends ApplicationEvent {
    private final Long gameId;
    private final Long gameSessionId;

    public GameEndEvent(Object source, Long gameId, Long gameSessionId) {
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
