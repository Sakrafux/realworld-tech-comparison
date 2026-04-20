package com.sakrafux.realworld.controller;

import com.sakrafux.realworld.dto.request.NewArticleRequest;
import com.sakrafux.realworld.dto.request.UpdateArticleRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    void getArticle_ValidSlug_ReturnsOkWithArticle() throws Exception {
        String token = registerUserViaApi("author", "author@example.com", "password123");
        createArticleViaApi(token, "Target Article", "Desc", "Body", List.of());

        mockMvc.perform(get("/articles/target-article"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.article.title").value("Target Article"));
    }

    @Test
    void updateArticle_AuthorUpdates_ReturnsOkWithUpdatedArticle() throws Exception {
        String token = registerUserViaApi("author", "author@example.com", "password123");
        createArticleViaApi(token, "Old Title", "Old Desc", "Old Body", List.of());

        UpdateArticleRequest updateRequest = UpdateArticleRequest.builder()
                .article(UpdateArticleRequest.ArticleData.builder()
                        .title("New Title")
                        .build())
                .build();

        mockMvc.perform(put("/articles/old-title")
                        .header("Authorization", "Token " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.article.title").value("New Title"))
                .andExpect(jsonPath("$.article.slug").value("new-title"));
    }

    @Test
    void deleteArticle_AuthorDeletes_ReturnsOk() throws Exception {
        String token = registerUserViaApi("author", "author@example.com", "password123");
        createArticleViaApi(token, "Delete Me", "Desc", "Body", List.of());

        mockMvc.perform(delete("/articles/delete-me")
                        .header("Authorization", "Token " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/articles/delete-me"))
                .andExpect(status().isNotFound());
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

    @Test
    void getArticlesFeed_ArticlesFromFollowedAuthors_ReturnsOkWithArticles() throws Exception {
        String authorToken = registerUserViaApi("author", "author@example.com", "password123");
        createArticleViaApi(authorToken, "Followed Article", "Desc", "Body", List.of());

        String userToken = registerUserViaApi("user", "user@example.com", "password123");

        // Follow author
        mockMvc.perform(post("/profiles/author/follow")
                        .header("Authorization", "Token " + userToken))
                .andExpect(status().isOk());

        // Get feed
        mockMvc.perform(get("/articles/feed")
                        .header("Authorization", "Token " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.articles", hasSize(1)))
                .andExpect(jsonPath("$.articles[0].title").value("Followed Article"));
    }

    @Test
    void favoriteArticle_ValidRequest_ReturnsOkWithFavoritedArticle() throws Exception {
        String authorToken = registerUserViaApi("author", "author@example.com", "password123");
        createArticleViaApi(authorToken, "Favorite Me", "Desc", "Body", List.of());

        String userToken = registerUserViaApi("user", "user@example.com", "password123");

        mockMvc.perform(post("/articles/favorite-me/favorite")
                        .header("Authorization", "Token " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.article.favorited").value(true))
                .andExpect(jsonPath("$.article.favoritesCount").value(1));
    }

    @Test
    void unfavoriteArticle_ValidRequest_ReturnsOkWithUnfavoritedArticle() throws Exception {
        String authorToken = registerUserViaApi("author", "author@example.com", "password123");
        createArticleViaApi(authorToken, "Unfavorite Me", "Desc", "Body", List.of());

        String userToken = registerUserViaApi("user", "user@example.com", "password123");

        // Favorite first
        mockMvc.perform(post("/articles/unfavorite-me/favorite")
                        .header("Authorization", "Token " + userToken))
                .andExpect(status().isOk());

        // Then unfavorite
        mockMvc.perform(delete("/articles/unfavorite-me/favorite")
                        .header("Authorization", "Token " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.article.favorited").value(false))
                .andExpect(jsonPath("$.article.favoritesCount").value(0));
    }
}
