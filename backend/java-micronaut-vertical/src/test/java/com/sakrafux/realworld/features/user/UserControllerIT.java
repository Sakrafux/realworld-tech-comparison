package com.sakrafux.realworld.features.user;

import com.sakrafux.realworld.features.user.dto.NewUserRequest;
import com.sakrafux.realworld.features.user.dto.UpdateUserRequest;
import com.sakrafux.realworld.features.user.dto.UserResponse;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class UserControllerIT {

    @Inject
    @Client("/")
    HttpClient httpClient;

    @Test
    void getCurrentUser_Authenticated_ReturnsUser() {
        String token = registerAndGetToken("currentuser", "current@example.com");

        HttpResponse<UserResponse> response = httpClient.toBlocking().exchange(
                HttpRequest.GET("/api/user").header("Authorization", "Token " + token), UserResponse.class);

        assertEquals(HttpStatus.OK, response.status());
        UserResponse body = response.body();
        assertNotNull(body);
        assertEquals("current@example.com", body.getUser().getEmail());
        assertEquals("currentuser", body.getUser().getUsername());
    }

    @Test
    void getCurrentUser_Unauthenticated_Returns401() {
        HttpClientResponseException ex = assertThrows(HttpClientResponseException.class, () ->
                httpClient.toBlocking().exchange(HttpRequest.GET("/api/user"), UserResponse.class));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }

    @Test
    void updateUser_ValidData_ReturnsUpdatedUser() {
        String token = registerAndGetToken("updateuser", "update@example.com");

        UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                .user(UpdateUserRequest.UserData.builder()
                        .bio("New bio")
                        .image("https://example.com/image.png")
                        .build())
                .build();

        HttpResponse<UserResponse> response = httpClient.toBlocking().exchange(
                HttpRequest.PUT("/api/user", updateRequest).header("Authorization", "Token " + token), UserResponse.class);

        assertEquals(HttpStatus.OK, response.status());
        UserResponse body = response.body();
        assertNotNull(body);
        assertEquals("New bio", body.getUser().getBio());
        assertEquals("https://example.com/image.png", body.getUser().getImage());
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
