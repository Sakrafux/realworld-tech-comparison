package com.sakrafux.realworld.user.application.port.in;

import com.sakrafux.realworld.user.domain.Profile;

public interface FollowUserUseCase {
    Profile follow(String targetUsername, String followerEmail);
}
