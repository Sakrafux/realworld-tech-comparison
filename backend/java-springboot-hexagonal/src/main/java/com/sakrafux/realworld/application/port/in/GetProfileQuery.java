package com.sakrafux.realworld.application.port.in;

import com.sakrafux.realworld.domain.model.Profile;

import java.util.Optional;

public interface GetProfileQuery {
    Profile getProfile(String username, Optional<String> observerEmail);
}
