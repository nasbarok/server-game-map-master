package com.airsoft.gamemapmaster.service.impl;

import com.airsoft.gamemapmaster.model.*;
import com.airsoft.gamemapmaster.model.DTO.InvitationDTO;
import com.airsoft.gamemapmaster.model.enums.InvitationStatus;
import com.airsoft.gamemapmaster.repository.*;
import com.airsoft.gamemapmaster.service.InvitationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.airsoft.gamemapmaster.service.ConnectedPlayerService;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public Invitation createInvitation(Long fieldId, Long targeted_userId, User sender) {
        Optional<Field> field = fieldRepository.findById(fieldId);
        Optional<User> user = userRepository.findById(targeted_userId);
        Invitation invitation = new Invitation();
        invitation.setTargetUser(user.get());
        invitation.setSender(sender);
        invitation.setStatus(InvitationStatus.PENDING);
        return invitationRepository.save(invitation);
    }

    @Override
    public List<Invitation> getInvitationsForUser(Long userId) {
        return invitationRepository.findByTargetUserId(userId);
    }

    @Override
    public List<Invitation> getPendingInvitationsForUser(Long userId) {
        return invitationRepository.findByTargetUserIdAndStatus(userId, InvitationStatus.PENDING);
    }

    @Override
    @Transactional
    public Optional<Invitation> acceptInvitation(Long invitationId, Long userId) {
        Optional<Invitation> invitation = invitationRepository.findById(invitationId);

        if (invitation.isPresent() && invitation.get().getTargetUser().getId().equals(userId)
                && "PENDING".equals(invitation.get().getStatus())) {
            Invitation invitationToUpdate = invitation.get();
            invitationToUpdate.setStatus(InvitationStatus.ACCEPTED);
            invitationToUpdate.setRespondedAt(OffsetDateTime.now(ZoneOffset.UTC));
            return Optional.of(invitationRepository.save(invitationToUpdate));
        }

        return Optional.empty();
    }

    @Override
    @Transactional
    public Optional<Invitation> declineInvitation(Long invitationId, Long userId) {
        Optional<Invitation> invitation = invitationRepository.findById(invitationId);

        if (invitation.isPresent() && invitation.get().getTargetUser().getId().equals(userId)
                && "PENDING".equals(invitation.get().getStatus())) {
            Invitation invitationToUpdate = invitation.get();
            invitationToUpdate.setStatus(InvitationStatus.DECLINED);
            invitationToUpdate.setRespondedAt(OffsetDateTime.now(ZoneOffset.UTC));
            return Optional.of(invitationRepository.save(invitationToUpdate));
        }

        return Optional.empty();
    }

    @Override
    public Optional<Invitation> getInvitationById(Long invitationId) {
        return invitationRepository.findById(invitationId);
    }

    /**
     * Créer ou récupérer une invitation existante (méthode idempotente)
     */
    public InvitationDTO createOrGetInvitation(Long fieldId, Long senderId, Long targetUserId) {
        // Vérifications de sécurité
        Field field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new RuntimeException("Terrain non trouvé"));

        if (field.getClosedAt()!=null) {
            throw new RuntimeException("Le terrain doit être ouvert pour envoyer des invitations");
        }

        // Vérifier que l'expéditeur est le propriétaire du terrain
        if (!field.getOwner().getId().equals(senderId)) {
            throw new RuntimeException("Seul le propriétaire du terrain peut envoyer des invitations");
        }

        // Vérifier que le destinataire existe
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Utilisateur destinataire non trouvé"));

        // Vérifier que l'expéditeur n'essaie pas de s'inviter lui-même
        if (senderId.equals(targetUserId)) {
            throw new RuntimeException("Vous ne pouvez pas vous inviter vous-même");
        }

        // Chercher une invitation existante
        Optional<Invitation> existingInvitation = invitationRepository
                .findByFieldIdAndSenderIdAndTargetUserId(fieldId, senderId, targetUserId);

        if (existingInvitation.isPresent()) {
            Invitation invitation = existingInvitation.get();

            // Si l'invitation est DECLINED ou EXPIRED, on peut la réactiver
            if (invitation.getStatus() == InvitationStatus.DECLINED ||
                    invitation.getStatus() == InvitationStatus.EXPIRED) {

                invitation.setStatus(InvitationStatus.PENDING);
                invitation.setRespondedAt(null);
                invitation = invitationRepository.save(invitation);
            }

            return InvitationDTO.fromEntity(invitation);
        }

        // Créer une nouvelle invitation
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Utilisateur expéditeur non trouvé"));

        Invitation newInvitation = new Invitation();
        newInvitation.setStatus(InvitationStatus.PENDING);
        newInvitation.setRespondedAt(null);
        newInvitation.setField(field);
        newInvitation.setSender(sender);
        newInvitation.setTargetUser(targetUser);
        newInvitation.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        newInvitation = invitationRepository.save(newInvitation);

        return InvitationDTO.fromEntity(newInvitation);
    }

    /**
     * Récupérer les invitations envoyées par un host pour un terrain
     */
    @Transactional(readOnly = true)
    public List<InvitationDTO> getSentInvitations(Long senderId, Long fieldId) {
        // Vérifier que le terrain existe et appartient à l'utilisateur
        Field field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new RuntimeException("Terrain non trouvé"));

        if (!field.getOwner().getId().equals(senderId)) {
            throw new RuntimeException("Vous ne pouvez voir que vos propres invitations");
        }

        List<Invitation> invitations = invitationRepository
                .findSentInvitationsByHostAndField(senderId, fieldId);

        return invitations.stream()
                .map(InvitationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les invitations reçues par un utilisateur
     */
    @Transactional
    public List<InvitationDTO> getReceivedInvitations(Long userId) {

        // 🧹 Supprime les invitations devenues caduques (terrain fermé)
        invitationRepository.deletePendingInvitationsOfClosedFields(userId);

        List<Invitation> invitations = invitationRepository
                .findPendingInvitationsByUserOnOpenFields(userId);

        return invitations.stream()
                .map(InvitationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Répondre à une invitation
     */
    public InvitationDTO respondToInvitation(Long invitationId, Long userId, boolean accepted) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation non trouvée"));

        // Vérifier que c'est bien le destinataire qui répond
        if (!invitation.getTargetUser().getId().equals(userId)) {
            throw new RuntimeException("Vous ne pouvez pas répondre à cette invitation");
        }

        // Vérifier que l'invitation est encore en attente
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new RuntimeException("Cette invitation n'est plus en attente de réponse");
        }

        // Vérifier que le terrain est encore ouvert
        if (invitation.getField().getClosedAt() != null) {
            throw new RuntimeException("Le terrain est fermé, l'invitation n'est plus valide");
        }

        // Mettre à jour le statut
        invitation.setStatus(accepted ? InvitationStatus.ACCEPTED : InvitationStatus.DECLINED);
        invitation.setRespondedAt(OffsetDateTime.now(ZoneOffset.UTC));
        invitation = invitationRepository.save(invitation);

        return InvitationDTO.fromEntity(invitation);
    }

    /**
     * Annuler une invitation
     */
    public void cancelInvitation(Long invitationId, Long senderId) {
        Optional<Invitation> invitationOptional = invitationRepository.findById(invitationId);
        if (!invitationOptional.isPresent()) {
            throw new RuntimeException("Invitation non trouvée");
        }
        Invitation invitation = invitationOptional.get();
        // Seul l’émetteur (ou un rôle admin si tu en as un) peut supprimer
        if (!invitation.getSender().getId().equals(senderId)) {
            throw new RuntimeException("Vous ne pouvez pas annuler cette invitation");
        }

        invitationRepository.delete(invitation); // hard delete
    }

    /**
     * Expirer toutes les invitations d'un terrain fermé
     */
    public int expireInvitationsForClosedField(Long fieldId) {
        return invitationRepository.expireInvitationsByField(fieldId, InvitationStatus.PENDING,InvitationStatus.EXPIRED);
    }

    /**
     * Compter les invitations en attente pour un host et un terrain
     */
    @Transactional(readOnly = true)
    public long countPendingInvitations(Long senderId, Long fieldId) {
        return invitationRepository.countInvitationsByHostAndFieldAndStatus(senderId, fieldId, InvitationStatus.PENDING);
    }

    /**
     * Compter les invitations reçues en attente pour un utilisateur
     */
    @Transactional(readOnly = true)
    public long countReceivedPendingInvitations(Long userId) {
        return invitationRepository.countInvitationsByUserAndStatus(userId, InvitationStatus.PENDING);
    }

    @Override
    public Optional<Invitation> findByFieldIdAndSenderIdAndTargetUserId(Long fieldId, Long senderId, Long targetUserId) {
        return invitationRepository.findByFieldIdAndSenderIdAndTargetUserId(fieldId, senderId, targetUserId);
    }

    @Override
    public void deletePendingInvitationsOfClosedFields(Long fieldId) {
        invitationRepository.deletePendingInvitationsOfClosedFields(fieldId);
    }
}
