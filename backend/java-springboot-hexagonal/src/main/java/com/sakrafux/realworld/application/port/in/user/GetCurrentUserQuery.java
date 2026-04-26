package com.sakrafux.realworld.application.port.in.user;

import com.sakrafux.realworld.domain.model.User;

public interface GetCurrentUserQuery {
    User getCurrentUser(String email);
}
