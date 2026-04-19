package com.sakrafux.realworld.controller;

import com.sakrafux.realworld.dto.response.UserResponse;
import com.sakrafux.realworld.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public UserResponse getCurrentUser() {
        // For now, we hardcode the email to test the vertical slice.
        // Once Spring Security is added, we'll get this from the SecurityContext.
        return userService.getCurrentUser("test@example.com");
    }
}
