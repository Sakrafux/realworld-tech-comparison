package com.sakrafux.realworld.features.article;

import com.sakrafux.realworld.features.article.dto.NewArticleRequest;
import com.sakrafux.realworld.features.user.dto.NewUserRequest;
import com.sakrafux.realworld.features.user.dto.UserResponse;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class TagsResourceIT {

    @Test
    public void getTags_ExistingTags_ReturnsTags() {
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

        given()
                .header("Authorization", "Token " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/articles")
                .then()
                .statusCode(201);

        given()
                .when()
                .get("/tags")
                .then()
                .statusCode(200)
                .body("tags", hasItem("tag1"))
                .body("tags", hasItem("tag2"));
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
