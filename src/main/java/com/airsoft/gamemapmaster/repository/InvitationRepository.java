package com.airsoft.gamemapmaster.repository;

import com.airsoft.gamemapmaster.model.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    
    List<Invitation> findByUserId(Long userId);
    
    List<Invitation> findByUserIdAndStatus(Long userId, String status);
    
}
