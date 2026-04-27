package com.sakrafux.realworld.user.application.port.api;

import lombok.Builder;

import java.util.Optional;

public interface AuthorProvider {
    AuthorResponse getAuthor(String username, Optional<String> observerEmail);

    @Builder
    record AuthorResponse(
        String username,
        String bio,
        String image,
        boolean following
    ) {}
}
