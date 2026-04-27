package com.sakrafux.realworld.user.application.port.in;

import com.sakrafux.realworld.user.domain.Profile;

import java.util.Optional;

public interface GetProfileQuery {
    Profile getProfile(String username, Optional<String> observerEmail);
}
