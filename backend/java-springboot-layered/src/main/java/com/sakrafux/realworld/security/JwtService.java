package com.sakrafux.realworld.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

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
     * @throws JwtException Invalid, expired or otherwise faulty JWT token
     * @throws IllegalArgumentException JWT claims string is empty
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