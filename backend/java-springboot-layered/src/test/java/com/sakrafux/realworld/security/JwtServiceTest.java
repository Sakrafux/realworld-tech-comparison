package com.sakrafux.realworld.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;
    private final String SECRET = "dGhpcy1pcy1hLXZlcnktc2VjdXJlLXNlY3JldC1rZXktZm9yLXVuaXQtdGVzdGluZw==";
    private final long EXPIRATION = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION);
    }

    @Test
    void generateToken_ValidEmail_ReturnsToken() {
        // Given
        String email = "test@example.com";

        // When
        String token = jwtService.generateToken(email);

        // Then
        assertThat(token).isNotBlank();
    }

    @Test
    void extractEmail_ValidToken_ReturnsEmail() {
        // Given
        String email = "test@example.com";
        String token = jwtService.generateToken(email);

        // When
        String extractedEmail = jwtService.extractEmail(token);

        // Then
        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    void extractEmail_InvalidToken_ThrowsJwtException() {
        // Given
        String invalidToken = "this.is.not.a.valid.token";

        // When / Then
        assertThatThrownBy(() -> jwtService.extractEmail(invalidToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void extractEmail_ExpiredToken_ThrowsJwtException() throws InterruptedException {
        // Given
        JwtService shortLivedJwtService = new JwtService(SECRET, 1); // 1ms expiration
        String email = "expired@example.com";
        String token = shortLivedJwtService.generateToken(email);

        // Small sleep to ensure expiration
        Thread.sleep(10);

        // When / Then
        assertThatThrownBy(() -> shortLivedJwtService.extractEmail(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void extractEmail_ModifiedToken_ThrowsJwtException() {
        // Given
        String email = "test@example.com";
        String token = jwtService.generateToken(email);
        String modifiedToken = token.substring(0, token.length() - 1) + (token.endsWith("a") ? "b" : "a");

        // When / Then
        assertThatThrownBy(() -> jwtService.extractEmail(modifiedToken))
                .isInstanceOf(JwtException.class);
    }
}
