package com.sakrafux.realworld.application.port.in.user;

import com.sakrafux.realworld.domain.model.User;
import lombok.Builder;

public interface RegisterUserUseCase {
    User registerUser(RegisterUserCommand command);

    @Builder
    record RegisterUserCommand(String username, String email, String password) {}
}
