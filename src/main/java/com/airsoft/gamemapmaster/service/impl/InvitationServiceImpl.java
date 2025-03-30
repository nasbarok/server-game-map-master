package com.airsoft.gamemapmaster.service.impl;

import com.airsoft.gamemapmaster.model.*;
import com.airsoft.gamemapmaster.repository.*;
import com.airsoft.gamemapmaster.service.InvitationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.airsoft.gamemapmaster.service.ConnectedPlayerService;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InvitationServiceImpl implements InvitationService {

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private ScenarioRepository scenarioRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private FieldRepository fieldRepository;
    @Autowired
    private ConnectedPlayerService connectedPlayerService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public Invitation createInvitation(Long fieldId, Long userId) {
        //@todo pas utilisé check et supprimer
        Optional<Field> field = fieldRepository.findById(fieldId);
        Optional<User> user = userRepository.findById(userId);
        Invitation invitation = new Invitation();
        invitation.setUser(user.get());
        invitation.setStatus("PENDING");
        return invitationRepository.save(invitation);
    }

    @Override
    public List<Invitation> getInvitationsForUser(Long userId) {
        return invitationRepository.findByUserId(userId);
    }

    @Override
    public List<Invitation> getPendingInvitationsForUser(Long userId) {
        return invitationRepository.findByUserIdAndStatus(userId, "PENDING");
    }

    @Override
    @Transactional
    public Optional<Invitation> acceptInvitation(Long invitationId, Long userId) {
        Optional<Invitation> invitation = invitationRepository.findById(invitationId);

        if (invitation.isPresent() && invitation.get().getUser().getId().equals(userId)
                && "PENDING".equals(invitation.get().getStatus())) {
            Invitation invitationToUpdate = invitation.get();
            invitationToUpdate.setStatus("ACCEPTED");
            invitationToUpdate.setRespondedAt(LocalDateTime.now());
            return Optional.of(invitationRepository.save(invitationToUpdate));
        }

        return Optional.empty();
    }

    @Override
    @Transactional
    public Optional<Invitation> declineInvitation(Long invitationId, Long userId) {
        Optional<Invitation> invitation = invitationRepository.findById(invitationId);

        if (invitation.isPresent() && invitation.get().getUser().getId().equals(userId)
                && "PENDING".equals(invitation.get().getStatus())) {
            Invitation invitationToUpdate = invitation.get();
            invitationToUpdate.setStatus("DECLINED");
            invitationToUpdate.setRespondedAt(LocalDateTime.now());
            return Optional.of(invitationRepository.save(invitationToUpdate));
        }

        return Optional.empty();
    }

    @Override
    @Transactional
    public boolean cancelInvitation(Long invitationId, Long userId) {
        Optional<Invitation> invitation = invitationRepository.findById(invitationId);

        if (invitation.isPresent()) {
            return true;
        }

        return false;
    }

    @Override
    public Optional<Invitation> getInvitationById(Long invitationId) {
        return invitationRepository.findById(invitationId);
    }
}
