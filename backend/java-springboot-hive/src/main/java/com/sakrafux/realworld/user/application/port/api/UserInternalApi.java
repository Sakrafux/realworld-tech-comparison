package com.sakrafux.realworld.user.application.port.api;

import com.sakrafux.realworld.user.domain.User;
import java.util.Optional;
import java.util.Set;

public interface UserInternalApi {
    Optional<User> getUserByEmail(String email);
    Optional<User> getUserByUsername(String username);
    Optional<User> getUserById(Long id);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Set<Long> getFollowingIds(Long userId);
}
