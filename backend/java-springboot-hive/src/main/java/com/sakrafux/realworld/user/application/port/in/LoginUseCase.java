package com.sakrafux.realworld.user.application.port.in;

import com.sakrafux.realworld.user.domain.User;
import lombok.Builder;

public interface LoginUseCase {
    User login(LoginCommand command);

    @Builder
    record LoginCommand(String email, String password) {}
}
