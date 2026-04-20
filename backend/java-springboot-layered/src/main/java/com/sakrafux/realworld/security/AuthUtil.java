package com.sakrafux.realworld.security;

import com.sakrafux.realworld.exception.UnauthorizedException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;
import java.util.Optional;

/**
 * Utility class for common security-related operations.
 * Provides helper methods to access the currently authenticated user's information.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthUtil {

    /**
     * Extracts the email (principal) of the currently authenticated user as an Optional.
     *
     * @return an Optional containing the user's email if authenticated, empty otherwise.
     */
    public static Optional<String> getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || Objects.equals(authentication.getPrincipal(), "anonymousUser")) {
            return Optional.empty();
        }
        return Optional.ofNullable((String) authentication.getPrincipal());
    }

    /**
     * Extracts the email (principal) of the currently authenticated user or throws UnauthorizedException.
     *
     * @return the user's email if authenticated.
     * @throws UnauthorizedException if no user is currently authenticated.
     */
    public static String getRequiredCurrentUserEmail() {
        return getCurrentUserEmail()
                .orElseThrow(() -> new UnauthorizedException("User must be authenticated"));
    }
}
