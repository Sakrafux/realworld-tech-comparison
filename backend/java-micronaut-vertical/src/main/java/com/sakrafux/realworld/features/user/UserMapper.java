package com.sakrafux.realworld.features.user;

import com.sakrafux.realworld.features.user.dto.ProfileResponse;
import com.sakrafux.realworld.features.user.dto.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "jakarta")
public interface UserMapper {

    default UserResponse toResponse(UserEntity userEntity, String token) {
        if (userEntity == null) {
            return null;
        }
        return UserResponse.builder()
                .user(toUserData(userEntity, token))
                .build();
    }

    @Mapping(target = "token", source = "token")
    @Mapping(target = "email", source = "userEntity.email")
    @Mapping(target = "username", source = "userEntity.username")
    @Mapping(target = "bio", source = "userEntity.bio")
    @Mapping(target = "image", source = "userEntity.image")
    UserResponse.UserData toUserData(UserEntity userEntity, String token);

    default ProfileResponse toProfileResponse(UserEntity userEntity, boolean following) {
        if (userEntity == null) {
            return null;
        }
        return ProfileResponse.builder()
                .profile(toProfileData(userEntity, following))
                .build();
    }

    @Mapping(target = "username", source = "userEntity.username")
    @Mapping(target = "bio", source = "userEntity.bio")
    @Mapping(target = "image", source = "userEntity.image")
    @Mapping(target = "following", source = "following")
    ProfileResponse.ProfileData toProfileData(UserEntity userEntity, boolean following);
}
