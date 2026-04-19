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

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {

    private final UserService userService;

    @PostMapping
    public UserResponse register(@Valid @RequestBody NewUserRequest request) {
        return userService.registerUser(request);
    }

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginUserRequest request) {
        return userService.loginUser(request);
    }
}
