package com.sakrafux.realworld.application.port.in.profile;

import com.sakrafux.realworld.domain.model.Profile;

public interface FollowUserUseCase {
    Profile follow(String targetUsername, String followerEmail);
}
