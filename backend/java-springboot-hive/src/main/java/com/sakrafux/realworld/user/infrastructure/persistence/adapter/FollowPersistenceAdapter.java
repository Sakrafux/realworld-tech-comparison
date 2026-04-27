package com.sakrafux.realworld.user.infrastructure.persistence.adapter;

import com.sakrafux.realworld.user.application.port.out.FollowRelationshipPort;
import com.sakrafux.realworld.user.infrastructure.persistence.repository.UserJpaRepository;
import com.sakrafux.realworld.user.infrastructure.persistence.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class FollowPersistenceAdapter implements FollowRelationshipPort {

    private final UserJpaRepository userJpaRepository;

    @Override
    public Set<Long> getFollowingIds(Long followerId) {
        return userJpaRepository.findById(followerId)
                .map(follower -> follower.getFollowing().stream()
                        .map(UserEntity::getId)
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }

    @Override
    public boolean isFollowing(Long followerId, Long followeeId) {
        return userJpaRepository.existsByIdAndFollowing_Id(followerId, followeeId);
    }

    @Override
    public void follow(Long followerId, Long followeeId) {
        userJpaRepository.findById(followerId).ifPresent(follower -> {
            userJpaRepository.findById(followeeId).ifPresent(followee -> {
                follower.getFollowing().add(followee);
                userJpaRepository.save(follower);
            });
        });
    }

    @Override
    public void unfollow(Long followerId, Long followeeId) {
        userJpaRepository.findById(followerId).ifPresent(follower -> {
            follower.getFollowing().removeIf(user -> user.getId().equals(followeeId));
            userJpaRepository.save(follower);
        });
    }
}
