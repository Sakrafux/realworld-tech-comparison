package com.sakrafux.realworld.user.infrastructure.web.mapper;

import com.sakrafux.realworld.user.domain.Profile;
import com.sakrafux.realworld.user.infrastructure.web.dto.response.ProfileResponse;
import org.mapstruct.Mapper;

@Mapper
public interface ProfileWebMapper {

    default ProfileResponse toResponse(Profile profile) {
        return ProfileResponse.builder()
                .profile(toProfileData(profile))
                .build();
    }

    ProfileResponse.ProfileData toProfileData(Profile profile);
}
