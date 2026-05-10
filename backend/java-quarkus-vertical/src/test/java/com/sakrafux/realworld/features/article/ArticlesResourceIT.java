package com.sakrafux.realworld.features.article;

import com.sakrafux.realworld.features.article.dto.NewArticleRequest;
import com.sakrafux.realworld.features.article.dto.UpdateArticleRequest;
import com.sakrafux.realworld.features.user.dto.NewUserRequest;
import com.sakrafux.realworld.features.user.dto.UserResponse;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
public class ArticlesResourceIT {

    @Test
    public void createArticle_ValidArticle_ReturnsCreated() {
        String token = registerAndGetToken("author1", "author1@example.com");
        NewArticleRequest request = NewArticleRequest.builder()
                .article(NewArticleRequest.ArticleData.builder()
                        .title("Quarkus Vertical Slice")
                        .description("How to build vertical slices in Quarkus")
                        .body("Content of the article")
                        .tagList(List.of("quarkus", "java"))
                        .build())
                .build();

        given()
                .header("Authorization", "Token " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/articles")
                .then()
                .statusCode(201)
                .body("article.title", is("Quarkus Vertical Slice"))
                .body("article.slug", is("quarkus-vertical-slice"))
                .body("article.author.username", is("author1"))
                .body("article.tagList", hasItems("quarkus", "java"));
    }

    @Test
    public void getArticle_ExistingSlug_ReturnsArticle() {
        String token = registerAndGetToken("author2", "author2@example.com");
        createArticle(token, "Existing Article");

        given()
                .when()
                .get("/articles/existing-article")
                .then()
                .statusCode(200)
                .body("article.title", is("Existing Article"))
                .body("article.slug", is("existing-article"));
    }

    @Test
    public void updateArticle_Author_ReturnsUpdated() {
        String token = registerAndGetToken("author3", "author3@example.com");
        createArticle(token, "To Update");

        UpdateArticleRequest updateRequest = UpdateArticleRequest.builder()
                .article(UpdateArticleRequest.ArticleData.builder()
                        .title("Updated Title")
                        .build())
                .build();

        given()
                .header("Authorization", "Token " + token)
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when()
                .put("/articles/to-update")
                .then()
                .statusCode(200)
                .body("article.title", is("Updated Title"))
                .body("article.slug", is("updated-title"));
    }

    @Test
    public void favoriteArticle_Authenticated_ReturnsFavorited() {
        String authorToken = registerAndGetToken("author4", "author4@example.com");
        String readerToken = registerAndGetToken("reader1", "reader1@example.com");
        createArticle(authorToken, "Favorite Me");

        given()
                .header("Authorization", "Token " + readerToken)
                .when()
                .post("/articles/favorite-me/favorite")
                .then()
                .statusCode(200)
                .body("article.favorited", is(true))
                .body("article.favoritesCount", is(1));
    }

    private void createArticle(String token, String title) {
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
