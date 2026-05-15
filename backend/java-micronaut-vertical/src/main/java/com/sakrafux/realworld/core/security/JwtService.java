package com.sakrafux.realworld.core.security;

import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.token.generator.TokenGenerator;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor
public class JwtService {

    private final TokenGenerator tokenGenerator;

    public String generateToken(String email) {
        Authentication authentication = Authentication.build(email);
        Optional<String> token = tokenGenerator.generateToken(authentication, null);
        return token.orElseThrow(() -> new RuntimeException("Failed to generate token"));
    }
}
