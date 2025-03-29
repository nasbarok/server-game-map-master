package com.airsoft.gamemapmaster.controller;

import com.airsoft.gamemapmaster.model.ConnectedPlayer;
import com.airsoft.gamemapmaster.model.GameMap;
import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.service.ConnectedPlayerService;
import com.airsoft.gamemapmaster.service.GameMapService;
import com.airsoft.gamemapmaster.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class PlayerConnectionControllerTest {

    @Mock
    private ConnectedPlayerService connectedPlayerService;

    @Mock
    private UserService userService;

    @Mock
    private GameMapService gameMapService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PlayerConnectionController controller;

    private User testUser;
    private GameMap testMap;
    private ConnectedPlayer testConnectedPlayer;

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

        testConnectedPlayer = new ConnectedPlayer();
        testConnectedPlayer.setId(1L);
        testConnectedPlayer.setUser(testUser);
        testConnectedPlayer.setGameMap(testMap);
        testConnectedPlayer.setActive(true);

        when(authentication.getName()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    }

    @Test
    public void testJoinMap_Success() {
        when(gameMapService.findById(1L)).thenReturn(Optional.of(testMap));
        when(connectedPlayerService.isPlayerConnectedToMap(1L, 1L)).thenReturn(false);
        when(connectedPlayerService.connectPlayerToMap(1L, 1L, null)).thenReturn(testConnectedPlayer);

        ResponseEntity<?> response = controller.joinMap(1L, null, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testConnectedPlayer, response.getBody());
    }

    @Test
    public void testJoinMap_AlreadyConnected() {
        when(gameMapService.findById(1L)).thenReturn(Optional.of(testMap));
        when(connectedPlayerService.isPlayerConnectedToMap(1L, 1L)).thenReturn(true);

        ResponseEntity<?> response = controller.joinMap(1L, null, authentication);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    public void testLeaveMap_Success() {
        when(connectedPlayerService.disconnectPlayerFromMap(1L, 1L)).thenReturn(true);

        ResponseEntity<?> response = controller.leaveMap(1L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testLeaveMap_NotConnected() {
        when(connectedPlayerService.disconnectPlayerFromMap(1L, 1L)).thenReturn(false);

        ResponseEntity<?> response = controller.leaveMap(1L, authentication);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testGetConnectedPlayers_Success() {
        List<ConnectedPlayer> players = new ArrayList<>();
        players.add(testConnectedPlayer);

        when(gameMapService.findById(1L)).thenReturn(Optional.of(testMap));
        when(connectedPlayerService.getConnectedPlayersByMapId(1L)).thenReturn(players);

        ResponseEntity<?> response = controller.getConnectedPlayers(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(players, response.getBody());
    }
}
