package com.sakrafux.realworld.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {
    @NotNull
    @Valid
    private UserData user;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserData {
        @Email
        @Size(max = 100)
        private String email;

        @Size(min = 8, max = 60)
        private String password;

        @Size(max = 50)
        private String username;

        private String bio;

        private String image;
    }
}
