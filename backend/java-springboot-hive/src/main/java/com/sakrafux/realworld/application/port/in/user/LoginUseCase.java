package com.sakrafux.realworld.application.port.in.user;

import com.sakrafux.realworld.domain.model.User;
import lombok.Builder;

public interface LoginUseCase {
    User login(LoginCommand command);

    @Builder
    record LoginCommand(String email, String password) {}
}
