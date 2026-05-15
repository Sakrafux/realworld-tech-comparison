package com.sakrafux.realworld.features.user.dto;

import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Serdeable
public class ProfileResponse {
    private ProfileData profile;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Serdeable
    public static class ProfileData {
        private String username;
        private String bio;
        private String image;
        private boolean following;
    }
}
