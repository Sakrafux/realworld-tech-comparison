package com.sakrafux.realworld.application.service;

import com.sakrafux.realworld.application.port.in.RegisterUserUseCase.RegisterUserCommand;
import com.sakrafux.realworld.application.port.out.PasswordEncoderPort;
import com.sakrafux.realworld.application.port.out.UserRepository;
import com.sakrafux.realworld.domain.exception.ResourceAlreadyExistsException;
import com.sakrafux.realworld.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private PasswordEncoderPort passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void registerUser_ValidCommand_SavesAndReturnsUser() {
        // Given
        RegisterUserCommand command = RegisterUserCommand.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.empty());
        given(userRepository.findByUsername("testuser")).willReturn(Optional.empty());
        given(passwordEncoder.encode("password123")).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));

        // When
        User result = userService.registerUser(command);

        // Then
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_ExistingEmail_ThrowsException() {
        // Given
        RegisterUserCommand command = RegisterUserCommand.builder()
                .email("existing@example.com")
                .build();

        given(userRepository.findByEmail("existing@example.com")).willReturn(Optional.of(new User()));

        // When / Then
        assertThatThrownBy(() -> userService.registerUser(command))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessage("Email already exists");
    }
}
