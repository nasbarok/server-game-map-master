package com.airsoft.gamemapmaster.service;

import com.airsoft.gamemapmaster.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> findAll();
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    User save(User user);
    void deleteById(Long id);

    List<User> searchUsersByUsernameOrEmail(String query);

    List<User> searchUsersByUsernameOrEmailExcludingCurrent(String query, String currentUsername);
}
