package com.sakrafux.realworld.user.application.port.api;

import com.sakrafux.realworld.user.domain.User;
import java.util.Optional;

public interface UserInternalApi {
    Optional<User> getUserByEmail(String email);
}
