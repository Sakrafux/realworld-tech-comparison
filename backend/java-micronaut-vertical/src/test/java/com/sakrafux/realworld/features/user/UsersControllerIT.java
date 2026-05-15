package com.sakrafux.realworld.features.user;

import com.sakrafux.realworld.core.exception.GenericErrorResponse;
import com.sakrafux.realworld.features.user.dto.LoginUserRequest;
import com.sakrafux.realworld.features.user.dto.NewUserRequest;
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
public class UsersControllerIT {

    @Inject
    @Client("/")
    HttpClient httpClient;

    @Test
    void registerUser_ValidUser_ReturnsCreated() {
        NewUserRequest request = NewUserRequest.builder()
                .user(NewUserRequest.UserData.builder()
                        .username("testuser")
                        .email("test@example.com")
                        .password("password123")
                        .build())
                .build();

        HttpResponse<UserResponse> response = httpClient.toBlocking().exchange(
                HttpRequest.POST("/api/users", request), UserResponse.class);

        assertEquals(HttpStatus.CREATED, response.status());
        UserResponse body = response.body();
        assertNotNull(body);
        assertEquals("testuser", body.getUser().getUsername());
        assertEquals("test@example.com", body.getUser().getEmail());
        assertNotNull(body.getUser().getToken());
    }

    @Test
    void registerUser_DuplicateEmail_Returns422() {
        NewUserRequest request = NewUserRequest.builder()
                .user(NewUserRequest.UserData.builder()
                        .username("user1")
                        .email("duplicate@example.com")
                        .password("password123")
                        .build())
                .build();

        // First one succeeds
        httpClient.toBlocking().exchange(HttpRequest.POST("/api/users", request), UserResponse.class);

        // Second one fails
        request.getUser().setUsername("user2");
        HttpClientResponseException ex = assertThrows(HttpClientResponseException.class, () ->
                httpClient.toBlocking().exchange(HttpRequest.POST("/api/users", request), UserResponse.class));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatus());
        GenericErrorResponse errorResponse = ex.getResponse().getBody(GenericErrorResponse.class).orElse(null);
        assertNotNull(errorResponse);
        assertTrue(errorResponse.getErrors().getBody().contains("Email already exists"));
    }

    @Test
    void loginUser_ValidCredentials_ReturnsUser() {
        NewUserRequest regRequest = NewUserRequest.builder()
                .user(NewUserRequest.UserData.builder()
                        .username("loginuser")
                        .email("login@example.com")
                        .password("password123")
                        .build())
                .build();

        httpClient.toBlocking().exchange(HttpRequest.POST("/api/users", regRequest), UserResponse.class);

        LoginUserRequest loginRequest = LoginUserRequest.builder()
                .user(LoginUserRequest.UserData.builder()
                        .email("login@example.com")
                        .password("password123")
                        .build())
                .build();

        HttpResponse<UserResponse> response = httpClient.toBlocking().exchange(
                HttpRequest.POST("/api/users/login", loginRequest), UserResponse.class);

        assertEquals(HttpStatus.OK, response.status());
        UserResponse body = response.body();
        assertNotNull(body);
        assertEquals("login@example.com", body.getUser().getEmail());
        assertNotNull(body.getUser().getToken());
    }

    @Test
    void loginUser_InvalidPassword_Returns401() {
        NewUserRequest regRequest = NewUserRequest.builder()
                .user(NewUserRequest.UserData.builder()
                        .username("wrongpassuser")
                        .email("wrongpass@example.com")
                        .password("password123")
                        .build())
                .build();

        httpClient.toBlocking().exchange(HttpRequest.POST("/api/users", regRequest), UserResponse.class);

        LoginUserRequest loginRequest = LoginUserRequest.builder()
                .user(LoginUserRequest.UserData.builder()
                        .email("wrongpass@example.com")
                        .password("wrongpassword")
                        .build())
                .build();

        HttpClientResponseException ex = assertThrows(HttpClientResponseException.class, () ->
                httpClient.toBlocking().exchange(HttpRequest.POST("/api/users/login", loginRequest), UserResponse.class));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }
}
