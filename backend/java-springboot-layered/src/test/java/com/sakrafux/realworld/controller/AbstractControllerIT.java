package com.sakrafux.realworld.controller;

import com.sakrafux.realworld.dto.request.NewArticleRequest;
import com.sakrafux.realworld.dto.request.NewUserRequest;
import com.sakrafux.realworld.repository.ArticleRepository;
import com.sakrafux.realworld.repository.CommentRepository;
import com.sakrafux.realworld.repository.TagRepository;
import com.sakrafux.realworld.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base class for controller integration tests.
 * Provides common configuration and helper methods for API operations.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class AbstractControllerIT {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ArticleRepository articleRepository;

    @Autowired
    protected TagRepository tagRepository;

    @Autowired
    protected CommentRepository commentRepository;

    /**
     * Cleans up all repositories before each test to ensure a fresh state.
     */
    @BeforeEach
    void cleanup() {
        commentRepository.deleteAll();
        articleRepository.deleteAll();
        tagRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * Registers a user via the API and returns their JWT token.
     *
     * @param username the username for registration
     * @param email    the email for registration
     * @param password the password for registration
     * @return the JWT token received from the registration response
     * @throws Exception if any error occurs during the API call
     */
    protected String registerUserViaApi(String username, String email, String password) throws Exception {
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
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("user").get("token").asString();
    }

    /**
     * Creates an article via the API.
     *
     * @param token       the JWT token of the author
     * @param title       the article title
     * @param description the article description
     * @param body        the article body
     * @param tags        the list of tags
     * @throws Exception if any error occurs
     */
    protected void createArticleViaApi(String token, String title, String description, String body, List<String> tags) throws Exception {
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
                .andExpect(status().isCreated());
    }
}
