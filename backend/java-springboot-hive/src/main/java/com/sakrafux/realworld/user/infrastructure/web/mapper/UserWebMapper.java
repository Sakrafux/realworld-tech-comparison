package com.sakrafux.realworld.user.infrastructure.web.mapper;

import com.sakrafux.realworld.user.application.port.in.LoginUseCase.LoginCommand;
import com.sakrafux.realworld.user.application.port.in.RegisterUserUseCase.RegisterUserCommand;
import com.sakrafux.realworld.user.application.port.in.UpdateUserUseCase.UpdateUserCommand;
import com.sakrafux.realworld.user.domain.User;
import com.sakrafux.realworld.user.infrastructure.web.dto.request.LoginUserRequest;
import com.sakrafux.realworld.user.infrastructure.web.dto.request.NewUserRequest;
import com.sakrafux.realworld.user.infrastructure.web.dto.request.UpdateUserRequest;
import com.sakrafux.realworld.user.infrastructure.web.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UserWebMapper {

    // Output Mapping (Domain -> Response DTO)
    default UserResponse toResponse(User user, String token) {
        return UserResponse.builder()
                .user(toUserData(user, token))
                .build();
    }

    @Mapping(target = "token", source = "token")
    UserResponse.UserData toUserData(User user, String token);

    // Input Mapping (Web DTO -> Application Command)
    @Mapping(target = "username", source = "request.user.username")
    @Mapping(target = "email", source = "request.user.email")
    @Mapping(target = "password", source = "request.user.password")
    RegisterUserCommand toRegisterCommand(NewUserRequest request);

    @Mapping(target = "email", source = "request.user.email")
    @Mapping(target = "password", source = "request.user.password")
    LoginCommand toLoginCommand(LoginUserRequest request);

    @Mapping(target = "currentEmail", source = "currentEmail")
    @Mapping(target = "email", source = "request.user.email")
    @Mapping(target = "username", source = "request.user.username")
    @Mapping(target = "password", source = "request.user.password")
    @Mapping(target = "bio", source = "request.user.bio")
    @Mapping(target = "image", source = "request.user.image")
    UpdateUserCommand toUpdateCommand(UpdateUserRequest request, String currentEmail);
}
