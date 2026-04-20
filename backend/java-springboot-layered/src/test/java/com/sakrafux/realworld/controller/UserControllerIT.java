package com.sakrafux.realworld.controller;

import com.sakrafux.realworld.dto.request.LoginUserRequest;
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

import static org.hamcrest.Matchers.notNullValue;
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
    void register_ValidUser_ReturnsOkWithUser() throws Exception {
        NewUserRequest request = createNewUserRequest("testuser", "test@example.com", "password123");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("testuser"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.token", notNullValue()));
    }

    @Test
    void login_ValidCredentials_ReturnsOkWithUser() throws Exception {
        // Register user via helper method instead of calling another test
        registerUserViaApi("testuser", "test@example.com", "password123");

        LoginUserRequest request = LoginUserRequest.builder()
                .user(LoginUserRequest.UserData.builder()
                        .email("test@example.com")
                        .password("password123")
                        .build())
                .build();

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.token", notNullValue()));
    }

    @Test
    void getCurrentUser_ValidToken_ReturnsOkWithUser() throws Exception {
        // Register and extract token using the helper
        String token = registerUserViaApi("testuser", "test@example.com", "password123");

        mockMvc.perform(get("/user")
                        .header("Authorization", "Token " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("testuser"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"));
    }

    @Test
    void register_DuplicateEmail_ReturnsUnprocessableEntity() throws Exception {
        register_ValidUser_ReturnsOkWithUser();

        NewUserRequest request = NewUserRequest.builder()
                .user(NewUserRequest.UserData.builder()
                        .username("different")
                        .email("test@example.com")
                        .password("password123")
                        .build())
                .build();

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errors.body[0]").value("Email already exists"));
    }

    @Test
    void login_WrongPassword_ReturnsUnauthorized() throws Exception {
        register_ValidUser_ReturnsOkWithUser();

        LoginUserRequest request = LoginUserRequest.builder()
                .user(LoginUserRequest.UserData.builder()
                        .email("test@example.com")
                        .password("wrongpassword")
                        .build())
                .build();

        mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errors.body[0]").value("Invalid email or password"));
    }

    @Test
    void register_InvalidEmail_ReturnsUnprocessableEntity() throws Exception {
        NewUserRequest request = createNewUserRequest("testuser", "invalid-email", "password123");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errors.body", notNullValue()));
    }

    @Test
    void register_BlankUsername_ReturnsUnprocessableEntity() throws Exception {
        NewUserRequest request = createNewUserRequest("", "test@example.com", "password123");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errors.body", notNullValue()));
    }

    @Test
    void register_PasswordTooShort_ReturnsUnprocessableEntity() throws Exception {
        NewUserRequest request = createNewUserRequest("testuser", "test@example.com", "short");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errors.body", notNullValue()));
    }

    @Test
    void login_BlankEmail_ReturnsUnprocessableEntity() throws Exception {
        LoginUserRequest request = LoginUserRequest.builder()
                .user(LoginUserRequest.UserData.builder()
                        .email("")
                        .password("password123")
                        .build())
                .build();

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errors.body", notNullValue()));
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

    // --- Private Helper Methods ---

    private NewUserRequest createNewUserRequest(String username, String email, String password) {
        return NewUserRequest.builder()
                .user(NewUserRequest.UserData.builder()
                        .username(username)
                        .email(email)
                        .password(password)
                        .build())
                .build();
    }

    /**
     * Helper to populate the database via the API and return the generated JWT.
     */
    private String registerUserViaApi(String username, String email, String password) throws Exception {
        NewUserRequest request = createNewUserRequest(username, email, password);

        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("user").get("token").asText();
    }
    }