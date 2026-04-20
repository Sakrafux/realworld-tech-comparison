package com.sakrafux.realworld.controller;

import com.sakrafux.realworld.dto.request.NewArticleRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link ArticlesController}.
 */
class ArticlesControllerIT extends AbstractControllerIT {

    @Test
    void createArticle_ValidRequest_ReturnsOkWithArticle() throws Exception {
        String token = registerUserViaApi("author", "author@example.com", "password123");

        NewArticleRequest request = NewArticleRequest.builder()
                .article(NewArticleRequest.ArticleData.builder()
                        .title("My Test Article")
                        .description("Test Description")
                        .body("Test Body")
                        .tagList(List.of("testing", "java"))
                        .build())
                .build();

        mockMvc.perform(post("/articles")
                        .header("Authorization", "Token " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.article.title").value("My Test Article"))
                .andExpect(jsonPath("$.article.slug").value("my-test-article"))
                .andExpect(jsonPath("$.article.tagList", hasSize(2)))
                .andExpect(jsonPath("$.article.author.username").value("author"));
    }

    @Test
    void getArticles_NoFilters_ReturnsOkWithArticles() throws Exception {
        String token = registerUserViaApi("author", "author@example.com", "password123");
        createArticleViaApi(token, "Article 1", "Desc", "Body", List.of());
        createArticleViaApi(token, "Article 2", "Desc", "Body", List.of());

        mockMvc.perform(get("/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.articles", hasSize(2)))
                .andExpect(jsonPath("$.articlesCount").value(2));
    }

    @Test
    void getArticles_FilterByTag_ReturnsOkWithFilteredArticles() throws Exception {
        String token = registerUserViaApi("author", "author@example.com", "password123");
        createArticleViaApi(token, "Java Article", "Desc", "Body", List.of("java"));
        createArticleViaApi(token, "Spring Article", "Desc", "Body", List.of("spring"));

        mockMvc.perform(get("/articles").param("tag", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.articles", hasSize(1)))
                .andExpect(jsonPath("$.articles[0].title").value("Java Article"));
    }
}
