package com.airsoft.gamemapmaster.repository;

import com.airsoft.gamemapmaster.model.Invitation;
import com.airsoft.gamemapmaster.model.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    List<Invitation> findByTargetUserId(Long targetUserId);

    List<Invitation> findByTargetUserIdAndStatus(Long targetUserId, InvitationStatus status);

    /**
     * Trouver une invitation existante pour éviter les doublons
     */
    @Query("select i from Invitation i join fetch i.sender s join fetch i.targetUser tu join fetch i.field f where f.id = :fieldId and s.id = :senderId and tu.id = :targetUserId")
    Optional<Invitation> findByFieldIdAndSenderIdAndTargetUserId(@Param("fieldId") Long fieldId,
                                                                 @Param("senderId") Long senderId,
                                                                 @Param("targetUserId") Long targetUserId);

    /**
     * Récupérer toutes les invitations envoyées par un host pour un terrain
     */
    @Query("SELECT i FROM Invitation i WHERE (i.sender.id = :senderId AND i.field.id = :fieldId) OR (i.targetUser.id = :senderId) ORDER BY i.createdAt DESC")
    List<Invitation> findSentInvitationsByHostAndField(
            @Param("senderId") Long senderId,
            @Param("fieldId") Long fieldId
    );

    /**
     * Récupérer les invitations reçues par un utilisateur (seulement PENDING)
     */
    @Query("SELECT i FROM Invitation i " +
            "WHERE i.targetUser.id = :userId AND i.status = 'PENDING' " +
            "ORDER BY i.createdAt DESC")
    List<Invitation> findPendingInvitationsByUser(@Param("userId") Long userId);

    @Query("SELECT i FROM Invitation i  WHERE i.targetUser.id = :userId AND i.status = :status ORDER BY i.createdAt DESC")
    List<Invitation> findInvitationsByUserAndStatus(
            @Param("userId") Long userId,
            @Param("status") InvitationStatus status
    );

    /**
     * Expirer toutes les invitations PENDING d'un terrain fermé
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE Invitation i SET i.status = :expired WHERE i.field.id = :fieldId AND i.status = :pending")
    int expireInvitationsByField(@Param("fieldId") Long fieldId,
                                 @Param("pending") InvitationStatus pending,
                                 @Param("expired") InvitationStatus expired);

    /**
     * Compter les invitations PENDING envoyées par un host pour un terrain
     */
    @Query("SELECT COUNT(i) FROM Invitation i WHERE i.sender.id = :senderId AND i.field.id = :fieldId AND i.status = :status")
    long countInvitationsByHostAndFieldAndStatus(
            @Param("senderId") Long senderId,
            @Param("fieldId") Long fieldId,
            @Param("status") InvitationStatus status
    );

    /**
     * Compter les invitations PENDING reçues par un utilisateur
     */
    @Query("SELECT COUNT(i) FROM Invitation i WHERE i.targetUser.id = :userId AND i.status = :status" +
            " AND i.field.closedAt IS NULL")
    long countInvitationsByUserAndStatus(
            @Param("userId") Long userId,
            @Param("status") InvitationStatus status
    );

    //Sélection uniquement sur terrains ouverts (+ fetch pour éviter le N+1 sur field/sender si utile)
    @Query("SELECT DISTINCT i FROM Invitation i " +
            "JOIN FETCH i.field f LEFT " +
            "JOIN FETCH i.sender s " +
            "WHERE i.targetUser.id = :userId " +
            "AND i.status = 'PENDING' " +
            "AND f.closedAt IS NULL " +
            "ORDER BY i.createdAt DESC")
    List<Invitation> findPendingInvitationsByUserOnOpenFields(@Param("userId") Long userId);

    //Nettoyage en masse des invitations PENDING dont le terrain est fermé
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM invitations i " +
            "WHERE i.target_user_id = :userId " +
            "AND i.status = 'PENDING' " +
            "AND field_id IN (SELECT id FROM fields WHERE closed_at IS NOT NULL)", nativeQuery = true)
    int deletePendingInvitationsOfClosedFieldsByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true) // (retire flushAutomatically)
    @Query(value = "DELETE FROM invitations " +
            "WHERE field_id = :fieldId", nativeQuery = true)
    void deletePendingInvitationsOfClosedFieldsByFieldId(Long fieldId);
}
