package com.airsoft.gamemapmaster.repository;

import com.airsoft.gamemapmaster.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByLeaderId(Long leaderId);

    @Query("SELECT t FROM Team t JOIN TeamMember tm ON tm.team = t WHERE tm.user.id =:userId")
    Team findTeamByUser(@Param("userId") Long userId);

    List<Team> findByGameMapId(Long mapId);
}
