package com.sakrafux.realworld.controller;

import com.sakrafux.realworld.dto.request.UpdateUserRequest;
import com.sakrafux.realworld.dto.response.UserResponse;
import com.sakrafux.realworld.security.AuthUtil;
import com.sakrafux.realworld.service.UserService;
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

    private final UserService userService;

    /**
     * Retrieves the profile of the currently authenticated user.
     * Maps to: GET /api/user
     *
     * @return a response containing the current user's details and JWT token
     */
    @GetMapping
    public UserResponse getCurrentUser() {
        String email = AuthUtil.getRequiredCurrentUserEmail();
        return userService.getCurrentUser(email);
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
        String currentEmail = AuthUtil.getRequiredCurrentUserEmail();
        return userService.updateUser(currentEmail, request);
    }
}
