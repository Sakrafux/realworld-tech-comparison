package com.sakrafux.realworld.controller;

import com.sakrafux.realworld.dto.request.NewArticleRequest;
import com.sakrafux.realworld.dto.request.NewUserRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    /**
     * Creates an article via the API.
     *
     * @param mockMvc      the MockMvc instance
     * @param objectMapper the ObjectMapper
     * @param token        the JWT token of the author
     * @param title        the article title
     * @param description  the article description
     * @param body         the article body
     * @param tags         the list of tags
     * @throws Exception if any error occurs
     */
    public static void createArticleViaApi(MockMvc mockMvc, ObjectMapper objectMapper, String token, String title, String description, String body, List<String> tags) throws Exception {
        NewArticleRequest request = NewArticleRequest.builder()
                .article(NewArticleRequest.ArticleData.builder()
                        .title(title)
                        .description(description)
                        .body(body)
                        .tagList(tags)
                        .build())
                .build();

        mockMvc.perform(post("/articles")
                        .header("Authorization", "Token " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
