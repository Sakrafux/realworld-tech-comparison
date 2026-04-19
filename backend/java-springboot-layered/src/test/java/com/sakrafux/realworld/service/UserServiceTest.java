package com.sakrafux.realworld.service;

import com.sakrafux.realworld.dto.request.LoginUserRequest;
import com.sakrafux.realworld.dto.request.NewUserRequest;
import com.sakrafux.realworld.dto.response.UserResponse;
import com.sakrafux.realworld.entity.UserEntity;
import com.sakrafux.realworld.exception.InvalidCredentialsException;
import com.sakrafux.realworld.exception.ResourceNotFoundException;
import com.sakrafux.realworld.exception.UserAlreadyExistsException;
import com.sakrafux.realworld.mapper.UserMapper;
import com.sakrafux.realworld.repository.UserRepository;
import com.sakrafux.realworld.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldRegisterNewUser() {
        // Given
        NewUserRequest request = NewUserRequest.builder()
                .user(NewUserRequest.UserData.builder()
                        .username("testuser")
                        .email("test@example.com")
                        .password("password123")
                        .build())
                .build();

        UserEntity user = UserEntity.builder()
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .build();

        UserResponse response = UserResponse.builder()
                .user(UserResponse.UserData.builder()
                        .username("testuser")
                        .email("test@example.com")
                        .build())
                .build();

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.empty());
        given(userRepository.findByUsername("testuser")).willReturn(Optional.empty());
        given(passwordEncoder.encode("password123")).willReturn("encodedPassword");
        given(userRepository.save(any(UserEntity.class))).willReturn(user);
        given(userMapper.toResponse(user)).willReturn(response);
        given(jwtService.generateToken("test@example.com")).willReturn("testToken");

        // When
        UserResponse result = userService.registerUser(request);

        // Then
        assertThat(result.getUser().getUsername()).isEqualTo("testuser");
        assertThat(result.getUser().getToken()).isEqualTo("testToken");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenRegisteringWithExistingEmail() {
        // Given
        NewUserRequest request = NewUserRequest.builder()
                .user(NewUserRequest.UserData.builder()
                        .email("test@example.com")
                        .build())
                .build();

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(new UserEntity()));

        // When / Then
        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Email already exists");
    }

    @Test
    void shouldLoginUserSuccessfully() {
        // Given
        LoginUserRequest request = LoginUserRequest.builder()
                .user(LoginUserRequest.UserData.builder()
                        .email("test@example.com")
                        .password("password123")
                        .build())
                .build();

        UserEntity user = UserEntity.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .build();

        UserResponse response = UserResponse.builder()
                .user(UserResponse.UserData.builder()
                        .email("test@example.com")
                        .build())
                .build();

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);
        given(userMapper.toResponse(user)).willReturn(response);
        given(jwtService.generateToken("test@example.com")).willReturn("testToken");

        // When
        UserResponse result = userService.loginUser(request);

        // Then
        assertThat(result.getUser().getEmail()).isEqualTo("test@example.com");
        assertThat(result.getUser().getToken()).isEqualTo("testToken");
    }

    @Test
    void shouldThrowExceptionWhenLoginUserNotFound() {
        // Given
        LoginUserRequest request = LoginUserRequest.builder()
                .user(LoginUserRequest.UserData.builder()
                        .email("notfound@example.com")
                        .build())
                .build();

        given(userRepository.findByEmail("notfound@example.com")).willReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> userService.loginUser(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldThrowExceptionWhenLoginWithWrongPassword() {
        // Given
        LoginUserRequest request = LoginUserRequest.builder()
                .user(LoginUserRequest.UserData.builder()
                        .email("test@example.com")
                        .password("wrongpassword")
                        .build())
                .build();

        UserEntity user = UserEntity.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .build();

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrongpassword", "encodedPassword")).willReturn(false);

        // When / Then
        assertThatThrownBy(() -> userService.loginUser(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void shouldGetCurrentUser() {
        // Given
        String email = "test@example.com";
        UserEntity user = UserEntity.builder().email(email).build();
        UserResponse response = UserResponse.builder()
                .user(UserResponse.UserData.builder().email(email).build())
                .build();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(userMapper.toResponse(user)).willReturn(response);
        given(jwtService.generateToken(email)).willReturn("testToken");

        // When
        UserResponse result = userService.getCurrentUser(email);

        // Then
        assertThat(result.getUser().getEmail()).isEqualTo(email);
        assertThat(result.getUser().getToken()).isEqualTo("testToken");
    }
}
