package com.airsoft.gamemapmaster.service.impl;

import com.airsoft.gamemapmaster.model.Invitation;
import com.airsoft.gamemapmaster.model.Scenario;
import com.airsoft.gamemapmaster.model.Team;
import com.airsoft.gamemapmaster.model.User;
import com.airsoft.gamemapmaster.repository.InvitationRepository;
import com.airsoft.gamemapmaster.repository.ScenarioRepository;
import com.airsoft.gamemapmaster.repository.TeamRepository;
import com.airsoft.gamemapmaster.repository.UserRepository;
import com.airsoft.gamemapmaster.service.InvitationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    public Invitation createInvitation(Long scenarioId, Long userId, Long teamId) {
        Optional<Scenario> scenario = scenarioRepository.findById(scenarioId);
        Optional<User> user = userRepository.findById(userId);
        
        if (scenario.isEmpty() || user.isEmpty()) {
            return null;
        }
        
        // Vérifier si une invitation est déjà en attente pour ce scénario et cet utilisateur
        Optional<Invitation> existingInvitation = invitationRepository.findByScenarioIdAndUserIdAndStatus(
                scenarioId, userId, "PENDING");
        
        if (existingInvitation.isPresent()) {
            return existingInvitation.get();
        }
        
        Invitation invitation = new Invitation();
        invitation.setScenario(scenario.get());
        invitation.setUser(user.get());
        invitation.setStatus("PENDING");
        
        // Assigner à une équipe si spécifiée
        if (teamId != null) {
            Optional<Team> team = teamRepository.findById(teamId);
            team.ifPresent(invitation::setTeam);
        }
        
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
    public List<Invitation> getInvitationsForScenario(Long scenarioId) {
        return invitationRepository.findByScenarioId(scenarioId);
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
            Scenario scenario = invitation.get().getScenario();
            
            // Vérifier si l'utilisateur est le créateur du scénario
            if (scenario.getGameMap().getCreator().getId().equals(userId)) {
                invitationRepository.deleteById(invitationId);
                return true;
            }
        }
        
        return false;
    }

    @Override
    public Optional<Invitation> getInvitationById(Long invitationId) {
        return invitationRepository.findById(invitationId);
    }
}
