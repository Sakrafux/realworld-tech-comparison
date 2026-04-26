package com.sakrafux.realworld.infrastructure.adapter.in.web.mapper;

import com.sakrafux.realworld.application.port.in.user.LoginUseCase;
import com.sakrafux.realworld.domain.model.User;
import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.request.LoginUserRequest;
import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.response.UserResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class UserWebMapperTest {

    private final UserWebMapper mapper = Mappers.getMapper(UserWebMapper.class);

    @Test
    void toLoginCommand_validRequest_returnsCommand() {
        // Given
        LoginUserRequest request = LoginUserRequest.builder()
                .user(LoginUserRequest.UserData.builder()
                        .email("test@example.com")
                        .password("password")
                        .build())
                .build();

        // When
        LoginUseCase.LoginCommand command = mapper.toLoginCommand(request);

        // Then
        assertThat(command.email()).isEqualTo("test@example.com");
        assertThat(command.password()).isEqualTo("password");
    }

    @Test
    void toResponse_validDomain_returnsResponse() {
        // Given
        User user = User.builder()
                .email("test@example.com")
                .username("testuser")
                .build();
        String token = "test-token";

        // When
        UserResponse response = mapper.toResponse(user, token);

        // Then
        assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");
        assertThat(response.getUser().getToken()).isEqualTo("test-token");
    }
}
