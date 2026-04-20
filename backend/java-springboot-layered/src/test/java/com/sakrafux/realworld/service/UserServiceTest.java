package com.sakrafux.realworld.service;

import com.sakrafux.realworld.dto.request.LoginUserRequest;
import com.sakrafux.realworld.dto.request.NewUserRequest;
import com.sakrafux.realworld.dto.request.UpdateUserRequest;
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
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
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

    @Spy
    private UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    @Test
    void registerUser_ValidUser_SavesAndReturnsUser() {
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

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.empty());
        given(userRepository.findByUsername("testuser")).willReturn(Optional.empty());
        given(passwordEncoder.encode("password123")).willReturn("encodedPassword");
        given(userRepository.save(any(UserEntity.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(jwtService.generateToken("test@example.com")).willReturn("testToken");

        // When
        UserResponse result = userService.registerUser(request);

        // Then
        assertThat(result.getUser().getUsername()).isEqualTo("testuser");
        assertThat(result.getUser().getEmail()).isEqualTo("test@example.com");
        assertThat(result.getUser().getToken()).isEqualTo("testToken");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void registerUser_ExistingEmail_ThrowsUserAlreadyExistsException() {
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
    void registerUser_ExistingUsername_ThrowsUserAlreadyExistsException() {
        // Given
        NewUserRequest request = NewUserRequest.builder()
                .user(NewUserRequest.UserData.builder()
                        .username("testuser")
                        .email("test@example.com")
                        .build())
                .build();

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.empty());
        given(userRepository.findByUsername("testuser")).willReturn(Optional.of(new UserEntity()));

        // When / Then
        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Username already exists");
    }

    @Test
    void loginUser_ValidCredentials_ReturnsUser() {
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

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);
        given(jwtService.generateToken("test@example.com")).willReturn("testToken");

        // When
        UserResponse result = userService.loginUser(request);

        // Then
        assertThat(result.getUser().getEmail()).isEqualTo("test@example.com");
        assertThat(result.getUser().getToken()).isEqualTo("testToken");
    }

    @Test
    void loginUser_UserNotFound_ThrowsResourceNotFoundException() {
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
    void loginUser_WrongPassword_ThrowsInvalidCredentialsException() {
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
    void getCurrentUser_UserNotFound_ThrowsResourceNotFoundException() {
        // Given
        String email = "notfound@example.com";
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> userService.getCurrentUser(email))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getCurrentUser_ExistingUser_ReturnsUser() {
        // Given
        String email = "test@example.com";
        UserEntity user = UserEntity.builder().email(email).build();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(jwtService.generateToken(email)).willReturn("testToken");

        // When
        UserResponse result = userService.getCurrentUser(email);

        // Then
        assertThat(result.getUser().getEmail()).isEqualTo(email);
        assertThat(result.getUser().getToken()).isEqualTo("testToken");
    }

    @Test
    void updateUser_ValidUpdate_ReturnsUpdatedUser() {
        // Given
        String currentEmail = "test@example.com";
        UpdateUserRequest request = UpdateUserRequest.builder()
                .user(UpdateUserRequest.UserData.builder()
                        .username("newusername")
                        .bio("new bio")
                        .build())
                .build();

        UserEntity user = UserEntity.builder()
                .email(currentEmail)
                .username("oldusername")
                .bio("old bio")
                .build();

        given(userRepository.findByEmail(currentEmail)).willReturn(Optional.of(user));
        given(userRepository.findByUsername("newusername")).willReturn(Optional.empty());
        given(userRepository.save(any(UserEntity.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(jwtService.generateToken(currentEmail)).willReturn("testToken");

        // When
        UserResponse result = userService.updateUser(currentEmail, request);

        // Then
        assertThat(result.getUser().getUsername()).isEqualTo("newusername");
        assertThat(result.getUser().getBio()).isEqualTo("new bio");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void updateUser_EmailAlreadyExists_ThrowsUserAlreadyExistsException() {
        // Given
        String currentEmail = "test@example.com";
        String newEmail = "existing@example.com";
        UpdateUserRequest request = UpdateUserRequest.builder()
                .user(UpdateUserRequest.UserData.builder()
                        .email(newEmail)
                        .build())
                .build();

        UserEntity user = UserEntity.builder().email(currentEmail).build();

        given(userRepository.findByEmail(currentEmail)).willReturn(Optional.of(user));
        given(userRepository.findByEmail(newEmail)).willReturn(Optional.of(new UserEntity()));

        // When / Then
        assertThatThrownBy(() -> userService.updateUser(currentEmail, request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Email already exists");
    }
}
