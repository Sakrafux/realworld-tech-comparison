package com.sakrafux.realworld.infrastructure.adapter.in.web.controller;

import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.request.LoginUserRequest;
import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.request.NewUserRequest;
import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.response.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for user authentication and registration.
 * Exposes endpoints for creating new users and logging in existing users.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {

    /**
     * Registers a new user.
     * Maps to: POST /api/users
     *
     * @param request the registration details (username, email, password)
     * @return a response containing the newly created user's details and a JWT token
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody NewUserRequest request) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Authenticates an existing user and returns a JWT token.
     * Maps to: POST /api/users/login
     *
     * @param request the login credentials (email, password)
     * @return a response containing the user's details and a new JWT token
     */
    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginUserRequest request) {
        throw new UnsupportedOperationException("TODO");
    }
}
