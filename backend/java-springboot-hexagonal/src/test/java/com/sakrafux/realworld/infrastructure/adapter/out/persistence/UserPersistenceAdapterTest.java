package com.sakrafux.realworld.infrastructure.adapter.out.persistence;

import com.sakrafux.realworld.domain.model.User;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.mapper.UserPersistenceMapper;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.repository.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserPersistenceAdapterTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    @Mock
    private UserPersistenceMapper userMapper;

    @InjectMocks
    private UserPersistenceAdapter userPersistenceAdapter;

    @Test
    void save_newUser_mapsAndSaves() {
        // Given
        User user = User.builder().username("new").build();
        UserEntity entity = new UserEntity();
        
        given(userMapper.toEntity(user)).willReturn(entity);
        given(userJpaRepository.save(entity)).willReturn(entity);
        given(userMapper.toDomain(entity)).willReturn(user);

        // When
        User result = userPersistenceAdapter.save(user);

        // Then
        assertThat(result).isNotNull();
        verify(userMapper).toEntity(user);
        verify(userJpaRepository).save(entity);
    }

    @Test
    void save_existingUser_loadsAndUpdatesEntity() {
        // Given
        User user = User.builder().id(1L).username("updated").build();
        UserEntity existingEntity = UserEntity.builder().id(1L).username("old").build();
        
        given(userJpaRepository.findById(1L)).willReturn(Optional.of(existingEntity));
        given(userJpaRepository.save(existingEntity)).willReturn(existingEntity);
        given(userMapper.toDomain(existingEntity)).willReturn(user);

        // When
        User result = userPersistenceAdapter.save(user);

        // Then
        assertThat(result).isNotNull();
        verify(userJpaRepository).findById(1L);
        verify(userMapper).updateEntityFromDomain(user, existingEntity);
        verify(userJpaRepository).save(existingEntity);
    }
}
