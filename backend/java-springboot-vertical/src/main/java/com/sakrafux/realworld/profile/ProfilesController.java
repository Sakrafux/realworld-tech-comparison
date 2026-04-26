package com.sakrafux.realworld.profile;

import com.sakrafux.realworld.profile.response.ProfileResponse;
import com.sakrafux.realworld.security.AuthUtil;
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

    private final ProfileService profileService;

    /**
     * Retrieves a user's profile by their username.
     * Auth is optional.
     *
     * @param username the username of the profile to retrieve
     * @return a ProfileResponse containing user profile information
     */
    @GetMapping("/{username}")
    public ProfileResponse getProfile(@PathVariable String username) {
        return profileService.getProfile(username, AuthUtil.getCurrentUserEmail());
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
        return profileService.followUser(username, AuthUtil.getRequiredCurrentUserEmail());
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
        return profileService.unfollowUser(username, AuthUtil.getRequiredCurrentUserEmail());
    }
}
