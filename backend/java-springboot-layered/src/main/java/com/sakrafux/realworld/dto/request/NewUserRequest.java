package com.sakrafux.realworld.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class NewUserRequest {
    @NotNull
    @Valid
    private UserData user;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserData {
        @NotBlank
        @Size(max = 50)
        private String username;

        @Email
        @NotBlank
        @Size(max = 100)
        private String email;

        @NotBlank
        @Size(min = 8, max = 60) // 60 is BCrypt length
        private String password;
    }
}
