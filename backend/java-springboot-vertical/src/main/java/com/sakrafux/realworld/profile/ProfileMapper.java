package com.sakrafux.realworld.profile;

import com.sakrafux.realworld.profile.response.ProfileResponse;
import com.sakrafux.realworld.user.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ProfileMapper {

    default ProfileResponse toResponse(UserEntity user, boolean following) {
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
