package com.sakrafux.realworld.features.article;

import com.sakrafux.realworld.features.article.dto.ArticleResponse;
import com.sakrafux.realworld.features.article.dto.NewArticleRequest;
import com.sakrafux.realworld.features.article.dto.UpdateArticleRequest;
import com.sakrafux.realworld.features.user.dto.NewUserRequest;
import com.sakrafux.realworld.features.user.dto.UserResponse;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class ArticlesControllerIT {

    @Inject
    @Client("/")
    HttpClient httpClient;

    @Test
    void createArticle_ValidArticle_ReturnsCreated() {
        String token = registerAndGetToken("author1", "author1@example.com");
        NewArticleRequest request = NewArticleRequest.builder()
                .article(NewArticleRequest.ArticleData.builder()
                        .title("Micronaut Vertical Slice")
                        .description("How to build vertical slices in Micronaut")
                        .body("Content of the article")
                        .tagList(List.of("micronaut", "java"))
                        .build())
                .build();

        HttpResponse<ArticleResponse> response = httpClient.toBlocking().exchange(
                HttpRequest.POST("/api/articles", request).header("Authorization", "Token " + token), ArticleResponse.class);

        assertEquals(HttpStatus.CREATED, response.status());
        ArticleResponse body = response.body();
        assertNotNull(body);
        assertEquals("Micronaut Vertical Slice", body.getArticle().getTitle());
        assertEquals("micronaut-vertical-slice", body.getArticle().getSlug());
        assertEquals("author1", body.getArticle().getAuthor().getUsername());
        assertTrue(body.getArticle().getTagList().containsAll(List.of("micronaut", "java")));
    }

    @Test
    void getArticle_ExistingSlug_ReturnsArticle() {
        String token = registerAndGetToken("author2", "author2@example.com");
        createArticle(token, "Existing Article");

        HttpResponse<ArticleResponse> response = httpClient.toBlocking().exchange(
                HttpRequest.GET("/api/articles/existing-article"), ArticleResponse.class);

        assertEquals(HttpStatus.OK, response.status());
        ArticleResponse body = response.body();
        assertNotNull(body);
        assertEquals("Existing Article", body.getArticle().getTitle());
        assertEquals("existing-article", body.getArticle().getSlug());
    }

    @Test
    void updateArticle_Author_ReturnsUpdated() {
        String token = registerAndGetToken("author3", "author3@example.com");
        createArticle(token, "To Update");

        UpdateArticleRequest updateRequest = UpdateArticleRequest.builder()
                .article(UpdateArticleRequest.ArticleData.builder()
                        .title("Updated Title")
                        .build())
                .build();

        HttpResponse<ArticleResponse> response = httpClient.toBlocking().exchange(
                HttpRequest.PUT("/api/articles/to-update", updateRequest).header("Authorization", "Token " + token), ArticleResponse.class);

        assertEquals(HttpStatus.OK, response.status());
        ArticleResponse body = response.body();
        assertNotNull(body);
        assertEquals("Updated Title", body.getArticle().getTitle());
        assertEquals("updated-title", body.getArticle().getSlug());
    }

    @Test
    void favoriteArticle_Authenticated_ReturnsFavorited() {
        String authorToken = registerAndGetToken("author4", "author4@example.com");
        String readerToken = registerAndGetToken("reader1", "reader1@example.com");
        createArticle(authorToken, "Favorite Me");

        HttpResponse<ArticleResponse> response = httpClient.toBlocking().exchange(
                HttpRequest.POST("/api/articles/favorite-me/favorite", "").header("Authorization", "Token " + readerToken), ArticleResponse.class);

        assertEquals(HttpStatus.OK, response.status());
        ArticleResponse body = response.body();
        assertNotNull(body);
        assertTrue(body.getArticle().isFavorited());
        assertEquals(1, body.getArticle().getFavoritesCount());
    }

    private void createArticle(String token, String title) {
        NewArticleRequest request = NewArticleRequest.builder()
                .article(NewArticleRequest.ArticleData.builder()
                        .title(title)
                        .description("desc")
                        .body("body")
                        .build())
                .build();

        httpClient.toBlocking().exchange(
                HttpRequest.POST("/api/articles", request).header("Authorization", "Token " + token), ArticleResponse.class);
    }

    private String registerAndGetToken(String username, String email) {
        NewUserRequest request = NewUserRequest.builder()
                .user(NewUserRequest.UserData.builder()
                        .username(username)
                        .email(email)
                        .password("password123")
                        .build())
                .build();

        HttpResponse<UserResponse> response = httpClient.toBlocking().exchange(
                HttpRequest.POST("/api/users", request), UserResponse.class);

        return response.body().getUser().getToken();
    }
}
