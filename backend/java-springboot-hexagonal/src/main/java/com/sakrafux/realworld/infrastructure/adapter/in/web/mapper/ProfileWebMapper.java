package com.sakrafux.realworld.infrastructure.adapter.in.web.mapper;

import com.sakrafux.realworld.domain.model.Profile;
import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.response.ProfileResponse;
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
