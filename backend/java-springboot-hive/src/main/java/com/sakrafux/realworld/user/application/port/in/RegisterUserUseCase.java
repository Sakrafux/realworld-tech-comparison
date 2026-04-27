package com.sakrafux.realworld.user.application.port.in;

import com.sakrafux.realworld.user.domain.User;
import lombok.Builder;

public interface RegisterUserUseCase {
    User registerUser(RegisterUserCommand command);

    @Builder
    record RegisterUserCommand(String username, String email, String password) {}
}
