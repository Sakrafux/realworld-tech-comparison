package com.sakrafux.realworld.infrastructure.adapter.out.persistence;

import com.sakrafux.realworld.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.repository.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FollowPersistenceAdapterTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    @InjectMocks
    private FollowPersistenceAdapter followPersistenceAdapter;

    @Test
    void isFollowing_callsRepository() {
        // Given
        given(userJpaRepository.existsByIdAndFollowing_Id(1L, 2L)).willReturn(true);

        // When
        boolean result = followPersistenceAdapter.isFollowing(1L, 2L);

        // Then
        assertThat(result).isTrue();
        verify(userJpaRepository).existsByIdAndFollowing_Id(1L, 2L);
    }

    @Test
    void follow_addsFolloweeToFollowerFollowingSet() {
        // Given
        UserEntity follower = UserEntity.builder().id(1L).following(new HashSet<>()).build();
        UserEntity followee = UserEntity.builder().id(2L).build();

        given(userJpaRepository.findById(1L)).willReturn(Optional.of(follower));
        given(userJpaRepository.findById(2L)).willReturn(Optional.of(followee));

        // When
        followPersistenceAdapter.follow(1L, 2L);

        // Then
        assertThat(follower.getFollowing()).contains(followee);
        verify(userJpaRepository).save(follower);
    }

    @Test
    void unfollow_removesFolloweeFromFollowerFollowingSet() {
        // Given
        UserEntity followee = UserEntity.builder().id(2L).build();
        UserEntity follower = UserEntity.builder().id(1L).following(new HashSet<>(Set.of(followee))).build();

        given(userJpaRepository.findById(1L)).willReturn(Optional.of(follower));

        // When
        followPersistenceAdapter.unfollow(1L, 2L);

        // Then
        assertThat(follower.getFollowing()).isEmpty();
        verify(userJpaRepository).save(follower);
    }
}
