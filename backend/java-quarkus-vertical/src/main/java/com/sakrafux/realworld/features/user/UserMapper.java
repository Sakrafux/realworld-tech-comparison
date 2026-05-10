package com.sakrafux.realworld.features.user;

import com.sakrafux.realworld.features.user.dto.ProfileResponse;
import com.sakrafux.realworld.features.user.dto.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
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

    default ProfileResponse toProfileResponse(UserEntity user, boolean following) {
        return ProfileResponse.builder()
                .profile(toProfileData(user, following))
                .build();
    }

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "bio", source = "user.bio")
    @Mapping(target = "image", source = "user.image")
    @Mapping(target = "following", source = "following")
    ProfileResponse.ProfileData toProfileData(UserEntity user, boolean following);
}
