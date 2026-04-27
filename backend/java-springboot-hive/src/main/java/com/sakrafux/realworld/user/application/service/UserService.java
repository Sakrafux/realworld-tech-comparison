package com.sakrafux.realworld.user.application.service;

import com.sakrafux.realworld.user.application.port.in.GetCurrentUserQuery;
import com.sakrafux.realworld.user.application.port.in.LoginUseCase;
import com.sakrafux.realworld.user.application.port.in.RegisterUserUseCase;
import com.sakrafux.realworld.user.application.port.in.UpdateUserUseCase;
import com.sakrafux.realworld.user.application.port.out.PasswordEncoderPort;
import com.sakrafux.realworld.user.application.port.out.UserRepository;
import com.sakrafux.realworld.core.exception.InvalidCredentialsException;
import com.sakrafux.realworld.core.exception.ResourceAlreadyExistsException;
import com.sakrafux.realworld.core.exception.ResourceNotFoundException;
import com.sakrafux.realworld.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements RegisterUserUseCase, LoginUseCase, GetCurrentUserQuery, UpdateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoder;

    @Override
    @Transactional
    public User registerUser(RegisterUserCommand command) {
        if (userRepository.findByEmail(command.email()).isPresent()) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }
        if (userRepository.findByUsername(command.username()).isPresent()) {
            throw new ResourceAlreadyExistsException("Username already exists");
        }

        User user = User.builder()
                .username(command.username())
                .email(command.email())
                .password(passwordEncoder.encode(command.password()))
                .bio("")
                .build();

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User login(LoginCommand command) {
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", command.email()));

        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    @Override
    @Transactional
    public User updateUser(UpdateUserCommand command) {
        User user = userRepository.findByEmail(command.currentEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", command.currentEmail()));

        if (command.email() != null && !command.email().equals(user.getEmail())) {
            if (userRepository.findByEmail(command.email()).isPresent()) {
                throw new ResourceAlreadyExistsException("Email already exists");
            }
        }

        if (command.username() != null && !command.username().equals(user.getUsername())) {
            if (userRepository.findByUsername(command.username()).isPresent()) {
                throw new ResourceAlreadyExistsException("Username already exists");
            }
        }

        user.update(
                command.email(),
                command.username(),
                command.password() != null ? passwordEncoder.encode(command.password()) : null,
                command.bio(),
                command.image()
        );

        return userRepository.save(user);
    }
}
