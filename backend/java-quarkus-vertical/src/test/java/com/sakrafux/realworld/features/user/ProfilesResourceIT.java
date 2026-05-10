package com.sakrafux.realworld.features.user;

import com.sakrafux.realworld.features.user.dto.NewUserRequest;
import com.sakrafux.realworld.features.user.dto.UserResponse;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class ProfilesResourceIT {

    @Test
    public void getProfile_ExistingUser_ReturnsProfile() {
        registerUser("profileuser", "profile@example.com");

        given()
                .when()
                .get("/profiles/profileuser")
                .then()
                .statusCode(200)
                .body("profile.username", is("profileuser"))
                .body("profile.following", is(false));
    }

    @Test
    public void followUser_ValidUser_ReturnsFollowingTrue() {
        registerUser("followed", "followed@example.com");
        String token = registerAndGetToken("follower", "follower@example.com");

        given()
                .header("Authorization", "Token " + token)
                .when()
                .post("/profiles/followed/follow")
                .then()
                .statusCode(200)
                .body("profile.username", is("followed"))
                .body("profile.following", is(true));

        // Verify with getProfile
        given()
                .header("Authorization", "Token " + token)
                .when()
                .get("/profiles/followed")
                .then()
                .statusCode(200)
                .body("profile.following", is(true));
    }

    @Test
    public void unfollowUser_ValidUser_ReturnsFollowingFalse() {
        registerUser("tounfollow", "tounfollow@example.com");
        String token = registerAndGetToken("unfollower", "unfollower@example.com");

        // Follow first
        given()
                .header("Authorization", "Token " + token)
                .when()
                .post("/profiles/tounfollow/follow")
                .then()
                .statusCode(200);

        // Then unfollow
        given()
                .header("Authorization", "Token " + token)
                .when()
                .delete("/profiles/tounfollow/follow")
                .then()
                .statusCode(200)
                .body("profile.username", is("tounfollow"))
                .body("profile.following", is(false));
    }

    private void registerUser(String username, String email) {
        NewUserRequest request = NewUserRequest.builder()
                .user(NewUserRequest.UserData.builder()
                        .username(username)
                        .email(email)
                        .password("password123")
                        .build())
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/users")
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
