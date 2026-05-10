package com.sakrafux.realworld.features.user;

import com.sakrafux.realworld.features.user.dto.NewUserRequest;
import com.sakrafux.realworld.features.user.dto.UpdateUserRequest;
import com.sakrafux.realworld.features.user.dto.UserResponse;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
public class UserResourceIT {

    @Test
    public void getCurrentUser_Authenticated_ReturnsUser() {
        String token = registerAndGetToken("currentuser", "current@example.com");

        given()
                .header("Authorization", "Token " + token)
                .when()
                .get("/user")
                .then()
                .statusCode(200)
                .body("user.email", is("current@example.com"))
                .body("user.username", is("currentuser"));
    }

    @Test
    public void getCurrentUser_Unauthenticated_Returns401() {
        given()
                .when()
                .get("/user")
                .then()
                .statusCode(401);
    }

    @Test
    public void updateUser_ValidData_ReturnsUpdatedUser() {
        String token = registerAndGetToken("updateuser", "update@example.com");

        UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                .user(UpdateUserRequest.UserData.builder()
                        .bio("New bio")
                        .image("https://example.com/image.png")
                        .build())
                .build();

        given()
                .header("Authorization", "Token " + token)
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when()
                .put("/user")
                .then()
                .statusCode(200)
                .body("user.bio", is("New bio"))
                .body("user.image", is("https://example.com/image.png"));
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
