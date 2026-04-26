package com.sakrafux.realworld.infrastructure.adapter.in.web.controller;

import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.response.ProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for managing user profiles.
 * Provides endpoints for retrieving profiles and following/unfollowing users.
 */
@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class ProfilesController {

    /**
     * Retrieves a user's profile by their username.
     * Auth is optional.
     *
     * @param username the username of the profile to retrieve
     * @return a ProfileResponse containing user profile information
     */
    @GetMapping("/{username}")
    public ProfileResponse getProfile(@PathVariable String username) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Follows a user by their username.
     * Auth is required.
     *
     * @param username the username of the user to follow
     * @return a ProfileResponse with following status set to true
     */
    @PostMapping("/{username}/follow")
    public ProfileResponse followUser(@PathVariable String username) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Unfollows a user by their username.
     * Auth is required.
     *
     * @param username the username of the user to unfollow
     * @return a ProfileResponse with following status set to false
     */
    @DeleteMapping("/{username}/follow")
    public ProfileResponse unfollowUser(@PathVariable String username) {
        throw new UnsupportedOperationException("TODO");
    }
}
