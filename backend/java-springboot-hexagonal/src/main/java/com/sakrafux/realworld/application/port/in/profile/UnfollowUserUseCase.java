package com.sakrafux.realworld.application.port.in.profile;

import com.sakrafux.realworld.domain.model.Profile;

public interface UnfollowUserUseCase {
    Profile unfollow(String targetUsername, String followerEmail);
}
