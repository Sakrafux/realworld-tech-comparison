package com.sakrafux.realworld.application.port.in;

import com.sakrafux.realworld.domain.model.Profile;

public interface UnfollowUserUseCase {
    Profile unfollow(String targetUsername, String followerEmail);
}
