package com.sakrafux.realworld.features.user;

import com.sakrafux.realworld.features.user.dto.LoginUserRequest;
import com.sakrafux.realworld.features.user.dto.NewUserRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
public class UsersResourceIT {

    @Test
    public void registerUser_ValidUser_ReturnsCreated() {
        NewUserRequest request = NewUserRequest.builder()
                .user(NewUserRequest.UserData.builder()
                        .username("testuser")
                        .email("test@example.com")
                        .password("password123")
                        .build())
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .body("user.username", is("testuser"))
                .body("user.email", is("test@example.com"))
                .body("user.token", notNullValue());
    }

    @Test
    public void registerUser_DuplicateEmail_Returns422() {
        NewUserRequest request = NewUserRequest.builder()
                .user(NewUserRequest.UserData.builder()
                        .username("user1")
                        .email("duplicate@example.com")
                        .password("password123")
                        .build())
                .build();

        // First one succeeds
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/users")
                .then()
                .statusCode(201);

        // Second one fails
        request.getUser().setUsername("user2");
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/users")
                .then()
                .statusCode(422)
                .body("errors.body[0]", is("Email already exists"));
    }

    @Test
    public void loginUser_ValidCredentials_ReturnsUser() {
        NewUserRequest regRequest = NewUserRequest.builder()
                .user(NewUserRequest.UserData.builder()
                        .username("loginuser")
                        .email("login@example.com")
                        .password("password123")
                        .build())
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(regRequest)
                .when()
                .post("/users")
                .then()
                .statusCode(201);

        LoginUserRequest loginRequest = LoginUserRequest.builder()
                .user(LoginUserRequest.UserData.builder()
                        .email("login@example.com")
                        .password("password123")
                        .build())
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/users/login")
                .then()
                .statusCode(200)
                .body("user.email", is("login@example.com"))
                .body("user.token", notNullValue());
    }

    @Test
    public void loginUser_InvalidPassword_Returns401() {
        NewUserRequest regRequest = NewUserRequest.builder()
                .user(NewUserRequest.UserData.builder()
                        .username("wrongpassuser")
                        .email("wrongpass@example.com")
                        .password("password123")
                        .build())
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(regRequest)
                .when()
                .post("/users")
                .then()
                .statusCode(201);

        LoginUserRequest loginRequest = LoginUserRequest.builder()
                .user(LoginUserRequest.UserData.builder()
                        .email("wrongpass@example.com")
                        .password("wrongpassword")
                        .build())
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/users/login")
                .then()
                .statusCode(401);
    }
}
