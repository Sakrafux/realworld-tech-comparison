package com.sakrafux.realworld.user.infrastructure.persistence.adapter;

import com.sakrafux.realworld.user.application.port.out.UserRepository;
import com.sakrafux.realworld.user.application.port.api.UserInternalPersistenceApi;
import com.sakrafux.realworld.core.exception.ResourceNotFoundException;
import com.sakrafux.realworld.user.domain.User;
import com.sakrafux.realworld.user.infrastructure.persistence.entity.UserEntity;
import com.sakrafux.realworld.user.infrastructure.persistence.mapper.UserPersistenceMapper;
import com.sakrafux.realworld.user.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepository, UserInternalPersistenceApi {

    private final UserJpaRepository userJpaRepository;
    private final UserPersistenceMapper userMapper;

    @Override
    public Optional<UserEntity> findEntityByUsername(String username) {
        return userJpaRepository.findByUsername(username);
    }

    @Override
    public Optional<UserEntity> findEntityById(Long id) {
        return userJpaRepository.findById(id);
    }

    @Override
    public Set<UserEntity> getFollowingEntities(String email) {
        return userJpaRepository.findByEmail(email)
                .map(UserEntity::getFollowing)
                .orElse(Collections.emptySet());
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userJpaRepository.findByUsername(username).map(userMapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity entity;
        if (user.getId() != null) {
            entity = userJpaRepository.findById(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", user.getId()));
            userMapper.updateEntityFromDomain(user, entity);
        } else {
            entity = userMapper.toEntity(user);
        }
        
        entity = userJpaRepository.save(entity);
        return userMapper.toDomain(entity);
    }
}
