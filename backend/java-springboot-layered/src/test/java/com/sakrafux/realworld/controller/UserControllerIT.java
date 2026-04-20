package com.sakrafux.realworld.controller;

import com.sakrafux.realworld.dto.request.UpdateUserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link UserController}.
 */
class UserControllerIT extends AbstractControllerIT {

    @Test
    void getCurrentUser_ValidToken_ReturnsOkWithUser() throws Exception {
        String token = registerUserViaApi("testuser", "test@example.com", "password123");

        mockMvc.perform(get("/user")
                        .header("Authorization", "Token " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("testuser"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"));
    }

    @Test
    void getCurrentUser_NoToken_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/user"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateUser_ValidRequest_ReturnsOkWithUpdatedUser() throws Exception {
        String token = registerUserViaApi("testuser", "test@example.com", "password123");

        UpdateUserRequest request = UpdateUserRequest.builder()
                .user(UpdateUserRequest.UserData.builder()
                        .username("newusername")
                        .bio("new bio")
                        .build())
                .build();

        mockMvc.perform(put("/user")
                        .header("Authorization", "Token " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("newusername"))
                .andExpect(jsonPath("$.user.bio").value("new bio"));
    }

    @Test
    void updateUser_DuplicateEmail_ReturnsUnprocessableEntity() throws Exception {
        registerUserViaApi("user2", "user2@example.com", "password123");
        String token = registerUserViaApi("testuser", "test@example.com", "password123");

        UpdateUserRequest request = UpdateUserRequest.builder()
                .user(UpdateUserRequest.UserData.builder()
                        .email("user2@example.com")
                        .build())
                .build();

        mockMvc.perform(put("/user")
                        .header("Authorization", "Token " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errors.body[0]").value("Email already exists"));
    }
}
