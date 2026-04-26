package com.sakrafux.realworld.infrastructure.adapter.in.web.controller;

import com.sakrafux.realworld.application.port.in.FollowUserUseCase;
import com.sakrafux.realworld.application.port.in.GetProfileQuery;
import com.sakrafux.realworld.application.port.in.UnfollowUserUseCase;
import com.sakrafux.realworld.domain.model.Profile;
import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.response.ProfileResponse;
import com.sakrafux.realworld.infrastructure.adapter.in.web.mapper.ProfileWebMapper;
import com.sakrafux.realworld.infrastructure.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class ProfilesController {

    private final GetProfileQuery getProfileQuery;
    private final FollowUserUseCase followUserUseCase;
    private final UnfollowUserUseCase unfollowUserUseCase;
    private final ProfileWebMapper profileWebMapper;

    @GetMapping("/{username}")
    public ProfileResponse getProfile(@PathVariable String username) {
        Profile profile = getProfileQuery.getProfile(username, AuthUtil.getCurrentUserEmail());
        return profileWebMapper.toResponse(profile);
    }

    @PostMapping("/{username}/follow")
    public ProfileResponse followUser(@PathVariable String username) {
        String followerEmail = AuthUtil.getRequiredCurrentUserEmail();
        Profile profile = followUserUseCase.follow(username, followerEmail);
        return profileWebMapper.toResponse(profile);
    }

    @DeleteMapping("/{username}/follow")
    public ProfileResponse unfollowUser(@PathVariable String username) {
        String followerEmail = AuthUtil.getRequiredCurrentUserEmail();
        Profile profile = unfollowUserUseCase.unfollow(username, followerEmail);
        return profileWebMapper.toResponse(profile);
    }
}
