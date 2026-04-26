package com.sakrafux.realworld.application.service;

import com.sakrafux.realworld.application.port.in.profile.FollowUserUseCase;
import com.sakrafux.realworld.application.port.in.profile.GetProfileQuery;
import com.sakrafux.realworld.application.port.in.profile.UnfollowUserUseCase;
import com.sakrafux.realworld.application.port.out.FollowRelationshipPort;
import com.sakrafux.realworld.application.port.out.UserRepository;
import com.sakrafux.realworld.domain.exception.ResourceNotFoundException;
import com.sakrafux.realworld.domain.model.Profile;
import com.sakrafux.realworld.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfileService implements GetProfileQuery, FollowUserUseCase, UnfollowUserUseCase {

    private final UserRepository userRepository;
    private final FollowRelationshipPort followRelationshipPort;

    @Override
    @Transactional(readOnly = true)
    public Profile getProfile(String username, Optional<String> observerEmail) {
        User targetUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        boolean following = observerEmail
                .flatMap(userRepository::findByEmail)
                .map(observer -> followRelationshipPort.isFollowing(observer.getId(), targetUser.getId()))
                .orElse(false);

        return mapToProfile(targetUser, following);
    }

    @Override
    @Transactional
    public Profile follow(String targetUsername, String followerEmail) {
        User targetUser = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", targetUsername));

        User follower = userRepository.findByEmail(followerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", followerEmail));

        if (!follower.getId().equals(targetUser.getId())) {
            followRelationshipPort.follow(follower.getId(), targetUser.getId());
        }

        return mapToProfile(targetUser, true);
    }

    @Override
    @Transactional
    public Profile unfollow(String targetUsername, String followerEmail) {
        User targetUser = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", targetUsername));

        User follower = userRepository.findByEmail(followerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", followerEmail));

        followRelationshipPort.unfollow(follower.getId(), targetUser.getId());

        return mapToProfile(targetUser, false);
    }

    private Profile mapToProfile(User user, boolean following) {
        return Profile.builder()
                .username(user.getUsername())
                .bio(user.getBio())
                .image(user.getImage())
                .following(following)
                .build();
    }
}
