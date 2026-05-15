package com.sakrafux.realworld.features.article;

import com.sakrafux.realworld.features.article.dto.NewArticleRequest;
import com.sakrafux.realworld.features.article.dto.TagsResponse;
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
public class TagsControllerIT {

    @Inject
    @Client("/")
    HttpClient httpClient;

    @Test
    void getTags_NoTags_ReturnsEmptyTags() {
        HttpResponse<TagsResponse> response = httpClient.toBlocking().exchange(
                HttpRequest.GET("/api/tags"), TagsResponse.class);

        assertEquals(HttpStatus.OK, response.status());
        TagsResponse body = response.body();
        assertNotNull(body);
        assertNotNull(body.getTags());
        assertEquals(0, body.getTags().size());
    }

    @Test
    void getTags_ExistingTags_ReturnsTags() {
        // Create an article with tags to populate tag table
        String token = registerAndGetToken("taguser", "tag@example.com");
        NewArticleRequest request = NewArticleRequest.builder()
                .article(NewArticleRequest.ArticleData.builder()
                        .title("Tag Article")
                        .description("desc")
                        .body("body")
                        .tagList(List.of("tag1", "tag2"))
                        .build())
                .build();

        httpClient.toBlocking().exchange(
                HttpRequest.POST("/api/articles", request).header("Authorization", "Token " + token), Object.class);

        HttpResponse<TagsResponse> response = httpClient.toBlocking().exchange(
                HttpRequest.GET("/api/tags"), TagsResponse.class);

        assertEquals(HttpStatus.OK, response.status());
        TagsResponse body = response.body();
        assertNotNull(body);
        assertTrue(body.getTags().contains("tag1"));
        assertTrue(body.getTags().contains("tag2"));
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
