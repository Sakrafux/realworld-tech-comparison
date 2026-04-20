package com.sakrafux.realworld.controller;

import com.sakrafux.realworld.dto.response.UserResponse;
import com.sakrafux.realworld.security.AuthUtil;
import com.sakrafux.realworld.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
