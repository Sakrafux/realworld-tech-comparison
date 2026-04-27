package com.sakrafux.realworld.user.application.port.out;

import com.sakrafux.realworld.user.domain.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    User save(User user);
}
