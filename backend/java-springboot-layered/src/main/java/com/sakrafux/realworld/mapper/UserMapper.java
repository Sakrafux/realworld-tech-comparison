package com.sakrafux.realworld.mapper;

import com.sakrafux.realworld.dto.response.UserResponse;
import com.sakrafux.realworld.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UserMapper {

    default UserResponse toResponse(UserEntity user, String token) {
        return UserResponse.builder()
                .user(toUserData(user, token))
                .build();
    }

    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "bio", source = "user.bio")
    @Mapping(target = "image", source = "user.image")
    @Mapping(target = "token", source = "token")
    UserResponse.UserData toUserData(UserEntity user, String token);
}
