package com.sakrafux.realworld.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private UserData user;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserData {
        private String email;
        private String token;
        private String username;
        private String bio;
        private String image;
    }
}
