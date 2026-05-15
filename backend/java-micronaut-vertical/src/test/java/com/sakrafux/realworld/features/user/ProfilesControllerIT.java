package com.sakrafux.realworld.features.user;

import com.sakrafux.realworld.features.user.dto.NewUserRequest;
import com.sakrafux.realworld.features.user.dto.ProfileResponse;
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
public class ProfilesControllerIT {

    @Inject
    @Client("/")
    HttpClient httpClient;

    @Test
    void getProfile_ExistingUser_ReturnsProfile() {
        registerUser("profileuser", "profile@example.com");

        HttpResponse<ProfileResponse> response = httpClient.toBlocking().exchange(
                HttpRequest.GET("/api/profiles/profileuser"), ProfileResponse.class);

        assertEquals(HttpStatus.OK, response.status());
        ProfileResponse body = response.body();
        assertNotNull(body);
        assertEquals("profileuser", body.getProfile().getUsername());
        assertFalse(body.getProfile().isFollowing());
    }

    @Test
    void followUser_ValidUser_ReturnsFollowingTrue() {
        registerUser("followed", "followed@example.com");
        String token = registerAndGetToken("follower", "follower@example.com");

        HttpResponse<ProfileResponse> response = httpClient.toBlocking().exchange(
                HttpRequest.POST("/api/profiles/followed/follow", "")
                        .header("Authorization", "Token " + token), ProfileResponse.class);

        assertEquals(HttpStatus.OK, response.status());
        ProfileResponse body = response.body();
        assertNotNull(body);
        assertEquals("followed", body.getProfile().getUsername());
        assertTrue(body.getProfile().isFollowing());

        // Verify with getProfile
        HttpResponse<ProfileResponse> getResponse = httpClient.toBlocking().exchange(
                HttpRequest.GET("/api/profiles/followed")
                        .header("Authorization", "Token " + token), ProfileResponse.class);

        assertEquals(HttpStatus.OK, getResponse.status());
        assertTrue(getResponse.body().getProfile().isFollowing());
    }

    @Test
    void unfollowUser_ValidUser_ReturnsFollowingFalse() {
        registerUser("tounfollow", "tounfollow@example.com");
        String token = registerAndGetToken("unfollower", "unfollower@example.com");

        // Follow first
        httpClient.toBlocking().exchange(
                HttpRequest.POST("/api/profiles/tounfollow/follow", "")
                        .header("Authorization", "Token " + token), ProfileResponse.class);

        // Then unfollow
        HttpResponse<ProfileResponse> response = httpClient.toBlocking().exchange(
                HttpRequest.DELETE("/api/profiles/tounfollow/follow")
                        .header("Authorization", "Token " + token), ProfileResponse.class);

        assertEquals(HttpStatus.OK, response.status());
        ProfileResponse body = response.body();
        assertNotNull(body);
        assertEquals("tounfollow", body.getProfile().getUsername());
        assertFalse(body.getProfile().isFollowing());
    }

    private void registerUser(String username, String email) {
        NewUserRequest request = NewUserRequest.builder()
                .user(NewUserRequest.UserData.builder()
                        .username(username)
                        .email(email)
                        .password("password123")
                        .build())
                .build();

        httpClient.toBlocking().exchange(HttpRequest.POST("/api/users", request), UserResponse.class);
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
