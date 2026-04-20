package com.sakrafux.realworld.security;

import com.sakrafux.realworld.exception.UnauthorizedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthUtilTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserEmail_AuthenticatedUser_ReturnsEmail() {
        // Given
        String email = "test@example.com";
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                email, null, Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // When
        Optional<String> result = AuthUtil.getCurrentUserEmail();

        // Then
        assertThat(result).isPresent().contains(email);
    }

    @Test
    void getCurrentUserEmail_NotAuthenticated_ReturnsEmpty() {
        // Given
        SecurityContextHolder.getContext().setAuthentication(null);

        // When
        Optional<String> result = AuthUtil.getCurrentUserEmail();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getCurrentUserEmail_AnonymousUser_ReturnsEmpty() {
        // Given
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "anonymousUser", null, Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // When
        Optional<String> result = AuthUtil.getCurrentUserEmail();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getRequiredCurrentUserEmail_AuthenticatedUser_ReturnsEmail() {
        // Given
        String email = "test@example.com";
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                email, null, Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // When
        String result = AuthUtil.getRequiredCurrentUserEmail();

        // Then
        assertThat(result).isEqualTo(email);
    }

    @Test
    void getRequiredCurrentUserEmail_NotAuthenticated_ThrowsUnauthorizedException() {
        // Given
        SecurityContextHolder.getContext().setAuthentication(null);

        // When / Then
        assertThatThrownBy(AuthUtil::getRequiredCurrentUserEmail)
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("User must be authenticated");
    }
}
