package com.sakrafux.realworld.controller;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link ProfilesController}.
 */
class ProfilesControllerIT extends AbstractControllerIT {

    @Test
    void getProfile_UserExists_ReturnsOkWithProfile() throws Exception {
        registerUserViaApi("targetuser", "target@example.com", "password123");

        mockMvc.perform(get("/profiles/targetuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.username").value("targetuser"))
                .andExpect(jsonPath("$.profile.following").value(false));
    }

    @Test
    void getProfile_UserExistsAndFollowing_ReturnsOkWithProfileFollowingTrue() throws Exception {
        registerUserViaApi("targetuser", "target@example.com", "password123");
        String token = registerUserViaApi("follower", "follower@example.com", "password123");

        // Follow first
        mockMvc.perform(post("/profiles/targetuser/follow")
                        .header("Authorization", "Token " + token))
                .andExpect(status().isOk());

        // Then get profile
        mockMvc.perform(get("/profiles/targetuser")
                        .header("Authorization", "Token " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.username").value("targetuser"))
                .andExpect(jsonPath("$.profile.following").value(true));
    }

    @Test
    void getProfile_UserNotFound_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/profiles/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void followUser_ValidRequest_ReturnsOkWithProfileFollowingTrue() throws Exception {
        registerUserViaApi("targetuser", "target@example.com", "password123");
        String token = registerUserViaApi("follower", "follower@example.com", "password123");

        mockMvc.perform(post("/profiles/targetuser/follow")
                        .header("Authorization", "Token " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.username").value("targetuser"))
                .andExpect(jsonPath("$.profile.following").value(true));
    }

    @Test
    void followUser_NoAuth_ReturnsUnauthorized() throws Exception {
        registerUserViaApi("targetuser", "target@example.com", "password123");

        mockMvc.perform(post("/profiles/targetuser/follow"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unfollowUser_ValidRequest_ReturnsOkWithProfileFollowingFalse() throws Exception {
        registerUserViaApi("targetuser", "target@example.com", "password123");
        String token = registerUserViaApi("follower", "follower@example.com", "password123");

        // Follow first
        mockMvc.perform(post("/profiles/targetuser/follow")
                        .header("Authorization", "Token " + token))
                .andExpect(status().isOk());

        // Then unfollow
        mockMvc.perform(delete("/profiles/targetuser/follow")
                        .header("Authorization", "Token " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.username").value("targetuser"))
                .andExpect(jsonPath("$.profile.following").value(false));
    }
}
