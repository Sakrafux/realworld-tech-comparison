package com.sakrafux.realworld.user.application.port.in;

import com.sakrafux.realworld.user.domain.Profile;

public interface UnfollowUserUseCase {
    Profile unfollow(String targetUsername, String followerEmail);
}
