package com.sakrafux.realworld.service;

import com.sakrafux.realworld.dto.response.ProfileResponse;
import com.sakrafux.realworld.entity.UserEntity;
import com.sakrafux.realworld.exception.ResourceNotFoundException;
import com.sakrafux.realworld.mapper.ProfileMapper;
import com.sakrafux.realworld.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service class responsible for managing user profiles and the following relationship between users.
 * Acts as an intermediary between the ProfilesController and UserRepository.
 */
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final ProfileMapper profileMapper;

    /**
     * Retrieves the profile of a user by their username.
     * Optionally checks if the currently authenticated user is following the target user.
     *
     * @param targetUsername the username of the profile to retrieve
     * @param currentEmail   an Optional containing the email of the currently authenticated user
     * @return a ProfileResponse containing the user's profile information and following status
     * @throws ResourceNotFoundException if no user is found with the provided username
     */
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String targetUsername, Optional<String> currentEmail) {
        UserEntity targetUser = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", targetUsername));

        Optional<UserEntity> currentUser = currentEmail.flatMap(userRepository::findByEmail);
        return getProfile(targetUser, currentUser);
    }

    /**
     * Retrieves the profile of a target user, considering the following status relative to the current user.
     *
     * @param targetUser  the user whose profile is to be retrieved
     * @param currentUser an Optional containing the currently authenticated user
     * @return a ProfileResponse containing the user's profile information and following status
     */
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(UserEntity targetUser, Optional<UserEntity> currentUser) {
        boolean following = currentUser
                .map(user -> user.getFollowing().contains(targetUser))
                .orElse(false);

        return profileMapper.toResponse(targetUser, following);
    }

    /**
     * Follows a user by their username.
     *
     * @param targetUsername the username of the profile to follow
     * @param currentEmail   the email of the currently authenticated user
     * @return a ProfileResponse containing the updated profile information with following status set to true
     * @throws ResourceNotFoundException if either the target user or the current user cannot be found
     */
    @Transactional
    public ProfileResponse followUser(String targetUsername, String currentEmail) {
        UserEntity targetUser = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", targetUsername));

        UserEntity currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        if (!currentUser.equals(targetUser)) {
            currentUser.getFollowing().add(targetUser);
            userRepository.save(currentUser);
        }

        return profileMapper.toResponse(targetUser, true);
    }

    /**
     * Unfollows a user by their username.
     *
     * @param targetUsername the username of the profile to unfollow
     * @param currentEmail   the email of the currently authenticated user
     * @return a ProfileResponse containing the updated profile information with following status set to false
     * @throws ResourceNotFoundException if either the target user or the current user cannot be found
     */
    @Transactional
    public ProfileResponse unfollowUser(String targetUsername, String currentEmail) {
        UserEntity targetUser = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", targetUsername));

        UserEntity currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        currentUser.getFollowing().remove(targetUser);
        userRepository.save(currentUser);

        return profileMapper.toResponse(targetUser, false);
    }
}
