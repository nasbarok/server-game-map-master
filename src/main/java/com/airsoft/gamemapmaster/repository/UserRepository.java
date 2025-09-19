package com.airsoft.gamemapmaster.repository;

import com.airsoft.gamemapmaster.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String query, String query1);

    List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndUsernameNot(String query, String query1, String currentUsername);
}
