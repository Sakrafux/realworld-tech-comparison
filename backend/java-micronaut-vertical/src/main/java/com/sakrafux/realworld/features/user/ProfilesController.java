package com.sakrafux.realworld.features.user;

import com.sakrafux.realworld.features.user.dto.ProfileResponse;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.Optional;

@Controller("/profiles")
@RequiredArgsConstructor
public class ProfilesController {

    private final UserService userService;

    @Get("/{username}")
    @Secured(SecurityRule.IS_ANONYMOUS)
    public ProfileResponse getProfile(String username, @Nullable Principal principal) {
        return userService.getProfile(username, Optional.ofNullable(principal).map(Principal::getName));
    }

    @Post("/{username}/follow")
    @Secured(SecurityRule.IS_AUTHENTICATED)
    public ProfileResponse follow(String username, Principal principal) {
        return userService.follow(username, principal.getName());
    }

    @Delete("/{username}/follow")
    @Secured(SecurityRule.IS_AUTHENTICATED)
    public ProfileResponse unfollow(String username, Principal principal) {
        return userService.unfollow(username, principal.getName());
    }
}
