package com.sakrafux.realworld.features.comment;

import com.sakrafux.realworld.features.article.dto.NewArticleRequest;
import com.sakrafux.realworld.features.comment.dto.CommentResponse;
import com.sakrafux.realworld.features.comment.dto.NewCommentRequest;
import com.sakrafux.realworld.features.user.dto.NewUserRequest;
import com.sakrafux.realworld.features.user.dto.UserResponse;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
public class CommentsResourceIT {

    @Test
    public void addComment_Authenticated_ReturnsCreated() {
        String token = registerAndGetToken("commenter1", "commenter1@example.com");
        String articleSlug = "comment-article";
        createArticle(token, "Comment Article", articleSlug);

        NewCommentRequest request = NewCommentRequest.builder()
                .comment(NewCommentRequest.CommentData.builder()
                        .body("This is a test comment")
                        .build())
                .build();

        given()
                .header("Authorization", "Token " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/articles/" + articleSlug + "/comments")
                .then()
                .statusCode(200)
                .body("comment.body", is("This is a test comment"))
                .body("comment.author.username", is("commenter1"));
    }

    @Test
    public void getComments_ExistingArticle_ReturnsComments() {
        String token = registerAndGetToken("commenter2", "commenter2@example.com");
        String articleSlug = "get-comments-article";
        createArticle(token, "Get Comments Article", articleSlug);
        addComment(token, articleSlug, "Comment 1");
        addComment(token, articleSlug, "Comment 2");

        given()
                .when()
                .get("/articles/" + articleSlug + "/comments")
                .then()
                .statusCode(200)
                .body("comments.size()", is(2))
                .body("comments.body", hasItems("Comment 1", "Comment 2"));
    }

    @Test
    public void deleteComment_Author_Returns200() {
        String token = registerAndGetToken("commenter3", "commenter3@example.com");
        String articleSlug = "delete-comment-article";
        createArticle(token, "Delete Comment Article", articleSlug);
        
        CommentResponse response = addComment(token, articleSlug, "To Delete");
        Long commentId = response.getComment().getId();

        given()
                .header("Authorization", "Token " + token)
                .when()
                .delete("/articles/" + articleSlug + "/comments/" + commentId)
                .then()
                .statusCode(200);

        // Verify it's gone
        given()
                .when()
                .get("/articles/" + articleSlug + "/comments")
                .then()
                .statusCode(200)
                .body("comments.size()", is(0));
    }

    private CommentResponse addComment(String token, String slug, String body) {
        NewCommentRequest request = NewCommentRequest.builder()
                .comment(NewCommentRequest.CommentData.builder()
                        .body(body)
                        .build())
                .build();

        return given()
                .header("Authorization", "Token " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/articles/" + slug + "/comments")
                .then()
                .statusCode(200)
                .extract()
                .as(CommentResponse.class);
    }

    private void createArticle(String token, String title, String slug) {
        NewArticleRequest request = NewArticleRequest.builder()
                .article(NewArticleRequest.ArticleData.builder()
                        .title(title)
                        .description("desc")
                        .body("body")
                        .build())
                .build();

        given()
                .header("Authorization", "Token " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/articles")
                .then()
                .statusCode(201);
    }

    private String registerAndGetToken(String username, String email) {
        NewUserRequest request = NewUserRequest.builder()
                .user(NewUserRequest.UserData.builder()
                        .username(username)
                        .email(email)
                        .password("password123")
                        .build())
                .build();

        return given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .extract()
                .as(UserResponse.class)
                .getUser()
                .getToken();
    }
}
