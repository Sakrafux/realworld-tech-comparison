package com.sakrafux.realworld.controller;

import com.sakrafux.realworld.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProfilesControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void getProfile_UserExists_ReturnsOkWithProfile() throws Exception {
        ControllerTestUtils.registerUserViaApi(mockMvc, objectMapper, "targetuser", "target@example.com", "password123");

        mockMvc.perform(get("/profiles/targetuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.username").value("targetuser"))
                .andExpect(jsonPath("$.profile.following").value(false));
    }

    @Test
    void getProfile_UserExistsAndFollowing_ReturnsOkWithProfileFollowingTrue() throws Exception {
        ControllerTestUtils.registerUserViaApi(mockMvc, objectMapper, "targetuser", "target@example.com", "password123");
        String token = ControllerTestUtils.registerUserViaApi(mockMvc, objectMapper, "follower", "follower@example.com", "password123");

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
        ControllerTestUtils.registerUserViaApi(mockMvc, objectMapper, "targetuser", "target@example.com", "password123");
        String token = ControllerTestUtils.registerUserViaApi(mockMvc, objectMapper, "follower", "follower@example.com", "password123");

        mockMvc.perform(post("/profiles/targetuser/follow")
                        .header("Authorization", "Token " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.username").value("targetuser"))
                .andExpect(jsonPath("$.profile.following").value(true));
    }

    @Test
    void followUser_NoAuth_ReturnsUnauthorized() throws Exception {
        ControllerTestUtils.registerUserViaApi(mockMvc, objectMapper, "targetuser", "target@example.com", "password123");

        mockMvc.perform(post("/profiles/targetuser/follow"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unfollowUser_ValidRequest_ReturnsOkWithProfileFollowingFalse() throws Exception {
        ControllerTestUtils.registerUserViaApi(mockMvc, objectMapper, "targetuser", "target@example.com", "password123");
        String token = ControllerTestUtils.registerUserViaApi(mockMvc, objectMapper, "follower", "follower@example.com", "password123");

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
