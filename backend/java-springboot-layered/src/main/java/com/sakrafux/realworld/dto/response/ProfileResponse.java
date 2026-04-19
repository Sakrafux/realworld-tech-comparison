package com.sakrafux.realworld.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponse {
    private ProfileData profile;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProfileData {
        private String username;
        private String bio;
        private String image;
        private boolean following;
    }
}
