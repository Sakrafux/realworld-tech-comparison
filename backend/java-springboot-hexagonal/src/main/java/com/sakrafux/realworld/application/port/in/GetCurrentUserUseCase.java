package com.sakrafux.realworld.application.port.in;

import com.sakrafux.realworld.domain.model.User;

public interface GetCurrentUserUseCase {
    User getCurrentUser(String email);
}
