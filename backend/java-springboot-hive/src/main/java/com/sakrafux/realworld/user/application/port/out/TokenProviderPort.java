package com.sakrafux.realworld.user.application.port.out;

public interface TokenProviderPort {
    String generateToken(String email);
}
