package com.sakrafux.realworld.infrastructure.adapter.in.web.controller;

import com.sakrafux.realworld.application.port.in.user.GetCurrentUserQuery;
import com.sakrafux.realworld.application.port.in.user.UpdateUserUseCase;
import com.sakrafux.realworld.application.port.out.TokenProviderPort;
import com.sakrafux.realworld.domain.model.User;
import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.request.UpdateUserRequest;
import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.response.UserResponse;
import com.sakrafux.realworld.infrastructure.adapter.in.web.mapper.UserWebMapper;
import com.sakrafux.realworld.infrastructure.security.AuthUtil;
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

    private final GetCurrentUserQuery getCurrentUserQuery;
    private final UpdateUserUseCase updateUserUseCase;
    private final TokenProviderPort tokenProviderPort;
    private final UserWebMapper userWebMapper;

    /**
     * Retrieves the profile of the currently authenticated user.
     * Maps to: GET /api/user
     *
     * @return a response containing the current user's details and JWT token
     */
    @GetMapping
    public UserResponse getCurrentUser() {
        String email = AuthUtil.getRequiredCurrentUserEmail();
        User user = getCurrentUserQuery.getCurrentUser(email);
        String token = tokenProviderPort.generateToken(user.getEmail());
        return userWebMapper.toResponse(user, token);
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
        User user = updateUserUseCase.updateUser(userWebMapper.toUpdateCommand(request, currentEmail));
        String token = tokenProviderPort.generateToken(user.getEmail());
        return userWebMapper.toResponse(user, token);
    }
}
