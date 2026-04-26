package com.sakrafux.realworld.infrastructure.adapter.in.web.controller;

import com.sakrafux.realworld.application.port.in.LoginUseCase;
import com.sakrafux.realworld.application.port.in.RegisterUserUseCase;
import com.sakrafux.realworld.application.port.out.TokenProviderPort;
import com.sakrafux.realworld.domain.model.User;
import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.request.LoginUserRequest;
import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.request.NewUserRequest;
import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.response.UserResponse;
import com.sakrafux.realworld.infrastructure.adapter.in.web.mapper.UserWebMapper;
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

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;
    private final TokenProviderPort tokenProviderPort;
    private final UserWebMapper userWebMapper;

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
        User user = registerUserUseCase.registerUser(userWebMapper.toRegisterCommand(request));
        String token = tokenProviderPort.generateToken(user.getEmail());
        return userWebMapper.toResponse(user, token);
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
        User user = loginUseCase.login(userWebMapper.toLoginCommand(request));
        String token = tokenProviderPort.generateToken(user.getEmail());
        return userWebMapper.toResponse(user, token);
    }
}
