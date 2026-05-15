package com.sakrafux.realworld.features.user.dto;

import io.micronaut.serde.annotation.Serdeable;
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
@Serdeable
public class UpdateUserRequest {
    @NotNull
    @Valid
    private UserData user;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Serdeable
    public static class UserData {
        @Email
        @Size(max = 100)
        private String email;
        @Size(max = 50)
        private String username;
        @Size(min = 8, max = 60)
        private String password;
        private String bio;
        private String image;
    }
}
