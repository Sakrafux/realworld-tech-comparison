package com.sakrafux.realworld.controller;

import com.sakrafux.realworld.dto.request.LoginUserRequest;
import com.sakrafux.realworld.dto.request.NewUserRequest;
import com.sakrafux.realworld.dto.response.UserResponse;
import com.sakrafux.realworld.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for user authentication and registration.
 * Exposes endpoints for creating new users and logging in existing users.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {

    private final UserService userService;

    /**
     * Registers a new user.
     * Maps to: POST /api/users
     *
     * @param request the registration details (username, email, password)
     * @return a response containing the newly created user's details and a JWT token
     */
    @PostMapping
    public UserResponse register(@Valid @RequestBody NewUserRequest request) {
        return userService.registerUser(request);
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
        return userService.loginUser(request);
    }
}
