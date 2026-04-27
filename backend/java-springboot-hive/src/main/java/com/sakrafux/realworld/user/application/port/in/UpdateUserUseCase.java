package com.sakrafux.realworld.user.application.port.in;

import com.sakrafux.realworld.user.domain.User;
import lombok.Builder;

public interface UpdateUserUseCase {
    User updateUser(UpdateUserCommand command);

    @Builder
    record UpdateUserCommand(String currentEmail, String email, String username, String password, String bio, String image) {}
}
