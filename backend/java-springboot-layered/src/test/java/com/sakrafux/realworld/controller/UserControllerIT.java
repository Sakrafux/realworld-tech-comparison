package com.sakrafux.realworld.controller;

import com.sakrafux.realworld.dto.request.NewUserRequest;
import com.sakrafux.realworld.dto.request.UpdateUserRequest;
import com.sakrafux.realworld.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerIT {

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

    private String registerUserViaApi(String username, String email, String password) throws Exception {
        NewUserRequest request = NewUserRequest.builder()
                .user(NewUserRequest.UserData.builder()
                        .username(username)
                        .email(email)
                        .password(password)
                        .build())
                .build();

        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("user").get("token").asText();
    }
}
