package com.sakrafux.realworld.user.application.port.api;

import com.sakrafux.realworld.user.infrastructure.persistence.entity.UserEntity;
import java.util.Optional;
import java.util.Set;

/**
 * Internal API for persistence-level access to User entities.
 * This is a compromise for JPA relationships between cells, but it still
 * routes access through a defined interface.
 */
public interface UserInternalPersistenceApi {
    Optional<UserEntity> findEntityByUsername(String username);
    Optional<UserEntity> findEntityById(Long id);
    Set<UserEntity> getFollowingEntities(String email);
}
