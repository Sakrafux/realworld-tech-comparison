package com.sakrafux.realworld.infrastructure.adapter.out.persistence;

import com.sakrafux.realworld.application.port.out.UserRepository;
import com.sakrafux.realworld.domain.model.User;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.mapper.UserPersistenceMapper;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final UserPersistenceMapper userMapper;

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
                    .orElseThrow(() -> new RuntimeException("User not found for update"));
            userMapper.updateEntityFromDomain(user, entity);
        } else {
            entity = userMapper.toEntity(user);
        }
        
        entity = userJpaRepository.save(entity);
        return userMapper.toDomain(entity);
    }
}
