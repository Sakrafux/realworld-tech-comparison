package com.sakrafux.realworld.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

/**
 * Service responsible for generating and validating JSON Web Tokens (JWT).
 * It uses a symmetric secret key (HMAC-SHA) defined in the application properties
 * to sign and verify the tokens.
 */
@Service
@Slf4j
public class JwtService {

    private final long expiration;
    private final SecretKey signingKey;

    // Constructor injection is preferred. The SecretKey is generated exactly once.
    public JwtService(
            @Value("${realworld.auth.secret}") String secret,
            @Value("${realworld.auth.token-expiration-msec}") long expiration
    ) {
        this.expiration = expiration;
        // Decode the Base64 string into a cryptographic key
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a new JWT token for a given user email.
     * The token includes the email as the subject, the issue date, and an expiration date.
     *
     * @param email the email of the user to generate the token for
     * @return the generated JWT token as a String
     */
    public String generateToken(String email) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiration))
                .signWith(signingKey) // Use the cached key
                .compact();
    }

    /**
     * Extracts the user's email (subject) from the given JWT token.
     * This method implicitly verifies the token's signature and expiration date.
     *
     * @param token the JWT token to parse
     * @return the user's email extracted from the token's subject claim
     * @throws JwtException if the token is invalid, expired, or otherwise faulty
     * @throws IllegalArgumentException if the JWT claims string is empty or null
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey) // Use the cached key
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}