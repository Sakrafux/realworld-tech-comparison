package com.sakrafux.realworld.core.security;

import jakarta.inject.Singleton;
import org.mindrot.jbcrypt.BCrypt;

@Singleton
public class PasswordService {

    public String encode(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public boolean matches(String password, String hashed) {
        return BCrypt.checkpw(password, hashed);
    }
}
