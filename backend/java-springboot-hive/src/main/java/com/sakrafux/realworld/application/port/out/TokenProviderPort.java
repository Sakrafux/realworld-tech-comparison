package com.sakrafux.realworld.application.port.out;

public interface TokenProviderPort {
    String generateToken(String email);
}
