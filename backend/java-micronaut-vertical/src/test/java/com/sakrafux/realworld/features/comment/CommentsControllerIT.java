package com.sakrafux.realworld.features.comment;

import com.sakrafux.realworld.features.article.dto.ArticleResponse;
import com.sakrafux.realworld.features.article.dto.NewArticleRequest;
import com.sakrafux.realworld.features.comment.dto.CommentResponse;
import com.sakrafux.realworld.features.comment.dto.MultipleCommentsResponse;
import com.sakrafux.realworld.features.comment.dto.NewCommentRequest;
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

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class CommentsControllerIT {

    @Inject
    @Client("/")
    HttpClient httpClient;

    @Test
    void addComment_Authenticated_ReturnsCreated() {
        String token = registerAndGetToken("commenter1", "commenter1@example.com");
        createArticle(token, "Article for Comment");

        NewCommentRequest request = NewCommentRequest.builder()
                .comment(NewCommentRequest.NewCommentData.builder()
                        .body("This is a test comment")
                        .build())
                .build();

        HttpResponse<CommentResponse> response = httpClient.toBlocking().exchange(
                HttpRequest.POST("/api/articles/article-for-comment/comments", request).header("Authorization", "Token " + token), CommentResponse.class);

        assertEquals(HttpStatus.OK, response.status());
        CommentResponse body = response.body();
        assertNotNull(body);
        assertEquals("This is a test comment", body.getComment().getBody());
        assertEquals("commenter1", body.getComment().getAuthor().getUsername());
    }

    @Test
    void getComments_ExistingArticle_ReturnsComments() {
        String token = registerAndGetToken("commenter2", "commenter2@example.com");
        createArticle(token, "Article for List");
        addComment(token, "article-for-list", "Comment 1");
        addComment(token, "article-for-list", "Comment 2");

        HttpResponse<MultipleCommentsResponse> response = httpClient.toBlocking().exchange(
                HttpRequest.GET("/api/articles/article-for-list/comments"), MultipleCommentsResponse.class);

        assertEquals(HttpStatus.OK, response.status());
        MultipleCommentsResponse body = response.body();
        assertNotNull(body);
        assertEquals(2, body.getComments().size());
    }

    @Test
    void deleteComment_Author_ReturnsOk() {
        String token = registerAndGetToken("commenter3", "commenter3@example.com");
        createArticle(token, "Article for Delete");
        CommentResponse commentResponse = addComment(token, "article-for-delete", "Delete me");
        Long commentId = commentResponse.getComment().getId();

        HttpResponse<?> response = httpClient.toBlocking().exchange(
                HttpRequest.DELETE("/api/articles/article-for-delete/comments/" + commentId).header("Authorization", "Token " + token));

        assertEquals(HttpStatus.OK, response.status());

        HttpResponse<MultipleCommentsResponse> listResponse = httpClient.toBlocking().exchange(
                HttpRequest.GET("/api/articles/article-for-delete/comments"), MultipleCommentsResponse.class);
        assertEquals(0, listResponse.body().getComments().size());
    }

    private CommentResponse addComment(String token, String slug, String body) {
        NewCommentRequest request = NewCommentRequest.builder()
                .comment(NewCommentRequest.NewCommentData.builder()
                        .body(body)
                        .build())
                .build();

        return httpClient.toBlocking().exchange(
                HttpRequest.POST("/api/articles/" + slug + "/comments", request).header("Authorization", "Token " + token), CommentResponse.class).body();
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
