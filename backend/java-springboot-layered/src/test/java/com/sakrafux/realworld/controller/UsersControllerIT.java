package com.sakrafux.realworld.controller;

import com.sakrafux.realworld.dto.request.LoginUserRequest;
import com.sakrafux.realworld.dto.request.NewUserRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UsersControllerIT {

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
    void registerUser_ValidUser_ReturnsOkWithUser() throws Exception {
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
    void registerUser_DuplicateEmail_ReturnsUnprocessableEntity() throws Exception {
        registerUserViaApi("testuser", "test@example.com", "password123");

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
        registerUserViaApi("testuser", "test@example.com", "password123");

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
    void registerUser_InvalidEmail_ReturnsUnprocessableEntity() throws Exception {
        NewUserRequest request = createNewUserRequest("testuser", "invalid-email", "password123");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errors.body", notNullValue()));
    }

    @Test
    void registerUser_BlankUsername_ReturnsUnprocessableEntity() throws Exception {
        NewUserRequest request = createNewUserRequest("", "test@example.com", "password123");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errors.body", notNullValue()));
    }

    @Test
    void registerUser_PasswordTooShort_ReturnsUnprocessableEntity() throws Exception {
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

    private NewUserRequest createNewUserRequest(String username, String email, String password) {
        return NewUserRequest.builder()
                .user(NewUserRequest.UserData.builder()
                        .username(username)
                        .email(email)
                        .password(password)
                        .build())
                .build();
    }

    private String registerUserViaApi(String username, String email, String password) throws Exception {
        NewUserRequest request = createNewUserRequest(username, email, password);

        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("user").get("token").asText();
    }
}
