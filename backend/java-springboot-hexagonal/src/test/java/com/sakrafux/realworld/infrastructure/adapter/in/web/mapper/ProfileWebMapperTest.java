package com.sakrafux.realworld.infrastructure.adapter.in.web.mapper;

import com.sakrafux.realworld.domain.model.Profile;
import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.response.ProfileResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileWebMapperTest {

    private final ProfileWebMapper mapper = Mappers.getMapper(ProfileWebMapper.class);

    @Test
    void toResponse_validDomain_returnsResponse() {
        // Given
        Profile profile = Profile.builder()
                .username("testuser")
                .bio("bio")
                .image("image")
                .following(true)
                .build();

        // When
        ProfileResponse response = mapper.toResponse(profile);

        // Then
        assertThat(response.getProfile().getUsername()).isEqualTo("testuser");
        assertThat(response.getProfile().isFollowing()).isTrue();
    }
}
