package com.sakrafux.realworld.mapper;

import com.sakrafux.realworld.dto.response.UserResponse;
import com.sakrafux.realworld.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UserMapper {

    @Mapping(target = "user.token", ignore = true) // Token is generated separately
    @Mapping(target = "user.email", source = "email")
    @Mapping(target = "user.username", source = "username")
    @Mapping(target = "user.bio", source = "bio")
    @Mapping(target = "user.image", source = "image")
    UserResponse toResponse(UserEntity entity);
}
