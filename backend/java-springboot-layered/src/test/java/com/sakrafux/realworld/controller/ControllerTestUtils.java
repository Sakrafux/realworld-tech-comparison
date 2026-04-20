package com.sakrafux.realworld.controller;

import com.sakrafux.realworld.dto.request.NewUserRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Utility class for controller integration tests.
 * Provides shared helper methods for common API operations.
 */
public class ControllerTestUtils {

    /**
     * Registers a user via the API and returns their JWT token.
     *
     * @param mockMvc      the MockMvc instance to use for the request
     * @param objectMapper the ObjectMapper for JSON processing
     * @param username     the username for registration
     * @param email        the email for registration
     * @param password     the password for registration
     * @return the JWT token received from the registration response
     * @throws Exception if any error occurs during the API call
     */
    public static String registerUserViaApi(MockMvc mockMvc, ObjectMapper objectMapper, String username, String email, String password) throws Exception {
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

        return objectMapper.readTree(response).get("user").get("token").asString();
    }
}
