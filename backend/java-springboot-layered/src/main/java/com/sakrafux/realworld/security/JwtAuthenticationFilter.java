package com.sakrafux.realworld.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * A custom servlet filter responsible for intercepting HTTP requests to validate
 * JSON Web Tokens (JWT) present in the Authorization header.
 * If a valid token is found, it extracts the user's email and populates the 
 * Spring SecurityContext, effectively authenticating the user for the current request.
 * This filter executes once per request.
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final String authHeader;
    private final String authPrefix;

    // Explicit constructor replaces @RequiredArgsConstructor to cleanly handle @Value injections
    public JwtAuthenticationFilter(
            JwtService jwtService,
            @Value("${realworld.auth.header}") String authHeader,
            @Value("${realworld.auth.prefix}") String authPrefix
    ) {
        this.jwtService = jwtService;
        this.authHeader = authHeader;
        this.authPrefix = authPrefix;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeaderValue = request.getHeader(authHeader);

        if (authHeaderValue == null || !authHeaderValue.startsWith(authPrefix)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeaderValue.substring(authPrefix.length());

        try {
            // This single call parses, verifies the signature, checks expiration, and extracts the email.
            final String email = jwtService.extractEmail(token);

            // If we reach here, the token is 100% valid.
            // We check if the authentication context is currently null to avoid
            // overriding an existing authentication or doing unnecessary work if the user
            // has already been authenticated (e.g., by another filter earlier in the chain).
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        email, null, Collections.emptyList()
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("Successfully authenticated user: {}", email);
            }
        } catch (JwtException | IllegalArgumentException e) {
            // Logs the failure at debug level so it doesn't pollute production INFO logs,
            // but remains available for debugging.
            log.debug("JWT Authentication failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}