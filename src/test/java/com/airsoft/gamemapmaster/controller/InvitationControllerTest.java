package com.airsoft.gamemapmaster.controller;

import com.airsoft.gamemapmaster.model.Invitation;
import com.airsoft.gamemapmaster.model.Scenario;
import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.service.InvitationService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class InvitationControllerTest {

    @Mock
    private InvitationService invitationService;

    @Mock
    private UserService userService;

    @Mock
    private ScenarioService scenarioService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private InvitationController controller;

    private User testUser;
    private Scenario testScenario;
    private Invitation testInvitation;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testScenario = new Scenario();
        testScenario.setId(1L);
        testScenario.setName("Test Scenario");

        testInvitation = new Invitation();
        testInvitation.setId(1L);
        testInvitation.setUser(testUser);
        testInvitation.setStatus("PENDING");

        when(authentication.getName()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    }

    @Test
    public void testGetMyInvitations_Success() {
        List<Invitation> invitations = new ArrayList<>();
        invitations.add(testInvitation);

        when(invitationService.getInvitationsForUser(1L)).thenReturn(invitations);

        ResponseEntity<?> response = controller.getMyInvitations(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(invitations, response.getBody());
    }

    @Test
    public void testGetMyPendingInvitations_Success() {
        List<Invitation> invitations = new ArrayList<>();
        invitations.add(testInvitation);

        when(invitationService.getPendingInvitationsForUser(1L)).thenReturn(invitations);

        ResponseEntity<?> response = controller.getMyPendingInvitations(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(invitations, response.getBody());
    }

    @Test
    public void testAcceptInvitation_Success() {
        when(invitationService.acceptInvitation(1L, 1L)).thenReturn(Optional.of(testInvitation));

        ResponseEntity<?> response = controller.acceptInvitation(1L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testInvitation, response.getBody());
    }

    @Test
    public void testAcceptInvitation_NotFound() {
        when(invitationService.acceptInvitation(1L, 1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.acceptInvitation(1L, authentication);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testDeclineInvitation_Success() {
        when(invitationService.declineInvitation(1L, 1L)).thenReturn(Optional.of(testInvitation));

        ResponseEntity<?> response = controller.declineInvitation(1L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testInvitation, response.getBody());
    }

    @Test
    public void testDeclineInvitation_NotFound() {
        when(invitationService.declineInvitation(1L, 1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.declineInvitation(1L, authentication);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
