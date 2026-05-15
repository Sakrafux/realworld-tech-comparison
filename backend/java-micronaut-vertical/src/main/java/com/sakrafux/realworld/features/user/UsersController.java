package com.sakrafux.realworld.features.user;

import com.sakrafux.realworld.features.user.dto.LoginUserRequest;
import com.sakrafux.realworld.features.user.dto.NewUserRequest;
import com.sakrafux.realworld.features.user.dto.UserResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Status;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller("/users")
@RequiredArgsConstructor
@Secured(SecurityRule.IS_ANONYMOUS)
public class UsersController {

    private final UserService userService;

    @Post
    @Status(HttpStatus.CREATED)
    public UserResponse register(@Valid @Body NewUserRequest request) {
        return userService.registerUser(request);
    }

    @Post("/login")
    public UserResponse login(@Valid @Body LoginUserRequest request) {
        return userService.loginUser(request);
    }
}
