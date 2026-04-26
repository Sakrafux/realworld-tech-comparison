package com.sakrafux.realworld.infrastructure.adapter.in.web.controller;

import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.request.UpdateUserRequest;
import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.response.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for operations related to the currently authenticated user.
 * Exposes endpoints for retrieving and updating the profile of the logged-in user.
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    /**
     * Retrieves the profile of the currently authenticated user.
     * Maps to: GET /api/user
     *
     * @return a response containing the current user's details and JWT token
     */
    @GetMapping
    public UserResponse getCurrentUser() {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Updates the profile of the currently authenticated user.
     * Maps to: PUT /api/user
     *
     * @param request the updated user details
     * @return a response containing the updated user's details and a new JWT token
     */
    @PutMapping
    public UserResponse updateUser(@Valid @RequestBody UpdateUserRequest request) {
        throw new UnsupportedOperationException("TODO");
    }
}
