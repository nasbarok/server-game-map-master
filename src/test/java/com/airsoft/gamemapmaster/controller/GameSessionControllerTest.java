package com.airsoft.gamemapmaster.controller;

import com.airsoft.gamemapmaster.model.GameMap;
import com.airsoft.gamemapmaster.model.Scenario;
import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.service.GameMapService;
import com.airsoft.gamemapmaster.service.ScenarioService;
import com.airsoft.gamemapmaster.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class GameSessionControllerTest {

    @Mock
    private GameMapService gameMapService;

    @Mock
    private ScenarioService scenarioService;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private GameSessionController controller;

    private User testUser;
    private GameMap testMap;
    private Scenario testScenario;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testMap = new GameMap();
        testMap.setId(1L);
        testMap.setName("Test Map");
        testMap.setOwner(testUser);

        testScenario = new Scenario();
        testScenario.setId(1L);
        testScenario.setName("Test Scenario");
        testScenario.setGameMap(testMap);

        when(authentication.getName()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    }

    @Test
    public void testStartGame_Success() {
        when(gameMapService.findById(1L)).thenReturn(Optional.of(testMap));
        when(scenarioService.findById(1L)).thenReturn(Optional.of(testScenario));

    }

    @Test
    public void testStartGame_NotOwner() {
        User otherUser = new User();
        otherUser.setId(2L);
        
        GameMap map = new GameMap();
        map.setId(1L);
        map.setOwner(otherUser);

        when(gameMapService.findById(1L)).thenReturn(Optional.of(map));


    }

    @Test
    public void testEndGame_Success() {
        when(gameMapService.findById(1L)).thenReturn(Optional.of(testMap));


    }

    @Test
    public void testEndGame_NotOwner() {
        User otherUser = new User();
        otherUser.setId(2L);
        
        GameMap map = new GameMap();
        map.setId(1L);
        map.setOwner(otherUser);

        when(gameMapService.findById(1L)).thenReturn(Optional.of(map));

    }
}
