package com.sakrafux.realworld.user;

import java.util.Collection;
import java.util.Optional;

public interface UserIntegrationService {
    Optional<Long> findUserIdByEmail(String email);
    Optional<Long> findUserIdByUsername(String username);
    Collection<Long> findFollowingIdsByEmail(String email);
}
