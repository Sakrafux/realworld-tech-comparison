package com.sakrafux.realworld.features.user;

import com.sakrafux.realworld.features.user.dto.UpdateUserRequest;
import com.sakrafux.realworld.features.user.dto.UserResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.security.Principal;

@Controller("/user")
@RequiredArgsConstructor
@Secured(SecurityRule.IS_AUTHENTICATED)
public class UserController {

    private final UserService userService;

    @Get
    public UserResponse getCurrentUser(Principal principal) {
        return userService.getCurrentUser(principal.getName());
    }

    @Put
    public UserResponse updateUser(Principal principal, @Valid @Body UpdateUserRequest request) {
        return userService.updateUser(principal.getName(), request);
    }
}
