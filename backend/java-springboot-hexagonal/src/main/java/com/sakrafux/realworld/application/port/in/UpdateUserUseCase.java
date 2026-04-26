package com.sakrafux.realworld.application.port.in;

import com.sakrafux.realworld.domain.model.User;
import lombok.Builder;

public interface UpdateUserUseCase {
    User updateUser(UpdateUserCommand command);

    @Builder
    record UpdateUserCommand(String currentEmail, String email, String username, String password, String bio, String image) {}
}
