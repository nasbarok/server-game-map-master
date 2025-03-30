package com.airsoft.gamemapmaster.service;

import com.airsoft.gamemapmaster.model.Invitation;
import com.airsoft.gamemapmaster.model.Scenario;
import com.airsoft.gamemapmaster.model.Team;
import com.airsoft.gamemapmaster.model.User;

import java.util.List;
import java.util.Optional;

public interface InvitationService {
    
    /**
     * Crée une invitation pour un utilisateur à rejoindre un scénario
     * @param userId ID de l'utilisateur
     * @return L'invitation créée
     */
    Invitation createInvitation(Long fieldId, Long userId);
    
    /**
     * Récupère toutes les invitations pour un utilisateur
     * @param userId ID de l'utilisateur
     * @return Liste des invitations
     */
    List<Invitation> getInvitationsForUser(Long userId);
    
    /**
     * Récupère toutes les invitations en attente pour un utilisateur
     * @param userId ID de l'utilisateur
     * @return Liste des invitations en attente
     */
    List<Invitation> getPendingInvitationsForUser(Long userId);

    
    /**
     * Accepte une invitation
     * @param invitationId ID de l'invitation
     * @param userId ID de l'utilisateur qui accepte l'invitation
     * @return L'invitation mise à jour
     */
    Optional<Invitation> acceptInvitation(Long invitationId, Long userId);
    
    /**
     * Refuse une invitation
     * @param invitationId ID de l'invitation
     * @param userId ID de l'utilisateur qui refuse l'invitation
     * @return L'invitation mise à jour
     */
    Optional<Invitation> declineInvitation(Long invitationId, Long userId);
    
    /**
     * Annule une invitation
     * @param invitationId ID de l'invitation
     * @param userId ID de l'utilisateur qui annule l'invitation (doit être le créateur du scénario)
     * @return true si l'invitation a été annulée, false sinon
     */
    boolean cancelInvitation(Long invitationId, Long userId);
    
    /**
     * Récupère une invitation par son ID
     * @param invitationId ID de l'invitation
     * @return L'invitation si elle existe
     */
    Optional<Invitation> getInvitationById(Long invitationId);
}
