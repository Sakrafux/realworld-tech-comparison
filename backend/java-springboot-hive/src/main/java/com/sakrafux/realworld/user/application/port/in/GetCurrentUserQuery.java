package com.sakrafux.realworld.user.application.port.in;

import com.sakrafux.realworld.user.domain.User;

public interface GetCurrentUserQuery {
    User getCurrentUser(String email);
}
