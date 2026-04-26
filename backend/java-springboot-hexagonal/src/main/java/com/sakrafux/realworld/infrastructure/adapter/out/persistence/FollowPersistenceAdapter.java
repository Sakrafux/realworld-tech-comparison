package com.sakrafux.realworld.infrastructure.adapter.out.persistence;

import com.sakrafux.realworld.application.port.out.FollowRelationshipPort;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FollowPersistenceAdapter implements FollowRelationshipPort {

    private final UserJpaRepository userJpaRepository;

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
