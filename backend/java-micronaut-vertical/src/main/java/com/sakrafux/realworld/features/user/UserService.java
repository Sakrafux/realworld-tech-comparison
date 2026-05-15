package com.sakrafux.realworld.features.user;

import com.sakrafux.realworld.core.exception.InvalidCredentialsException;
import com.sakrafux.realworld.core.exception.ResourceAlreadyExistsException;
import com.sakrafux.realworld.core.exception.ResourceNotFoundException;
import com.sakrafux.realworld.core.security.JwtService;
import com.sakrafux.realworld.core.security.PasswordService;
import com.sakrafux.realworld.features.user.dto.LoginUserRequest;
import com.sakrafux.realworld.features.user.dto.NewUserRequest;
import com.sakrafux.realworld.features.user.dto.UpdateUserRequest;
import com.sakrafux.realworld.features.user.dto.UserResponse;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.transaction.Transactional;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    @Transactional
    public UserResponse registerUser(NewUserRequest request) {
        var userData = request.getUser();

        if (userRepository.findByEmail(userData.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }
        if (userRepository.findByUsername(userData.getUsername()).isPresent()) {
            throw new ResourceAlreadyExistsException("Username already exists");
        }

        UserEntity user = UserEntity.builder()
                .username(userData.getUsername())
                .email(userData.getEmail())
                .password(passwordService.encode(userData.getPassword()))
                .bio("")
                .build();

        user = userRepository.save(user);
        String token = jwtService.generateToken(user.getEmail());
        return userMapper.toResponse(user, token);
    }

    @Transactional
    public UserResponse loginUser(LoginUserRequest request) {
        var userData = request.getUser();

        UserEntity user = userRepository.findByEmail(userData.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userData.getEmail()));

        if (!passwordService.matches(userData.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user.getEmail());
        return userMapper.toResponse(user, token);
    }

    @Transactional
    public UserResponse getCurrentUser(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        String token = jwtService.generateToken(user.getEmail());
        return userMapper.toResponse(user, token);
    }

    @Transactional
    public UserResponse updateUser(String currentEmail, UpdateUserRequest request) {
        UserEntity user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        UpdateUserRequest.UserData userData = request.getUser();

        if (userData.getEmail() != null && !userData.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(userData.getEmail()).isPresent()) {
                throw new ResourceAlreadyExistsException("Email already exists");
            }
            user.setEmail(userData.getEmail());
        }

        if (userData.getUsername() != null && !userData.getUsername().equals(user.getUsername())) {
            if (userRepository.findByUsername(userData.getUsername()).isPresent()) {
                throw new ResourceAlreadyExistsException("Username already exists");
            }
            user.setUsername(userData.getUsername());
        }

        if (userData.getPassword() != null) {
            user.setPassword(passwordService.encode(userData.getPassword()));
        }

        if (userData.getBio() != null) {
            user.setBio(userData.getBio());
        }

        if (userData.getImage() != null) {
            user.setImage(userData.getImage());
        }

        user = userRepository.save(user);
        String token = jwtService.generateToken(user.getEmail());
        return userMapper.toResponse(user, token);
    }

    public Optional<Long> findUserIdByEmail(String email) {
        return userRepository.findByEmail(email).map(UserEntity::getId);
    }

    public Optional<Long> findUserIdByUsername(String username) {
        return userRepository.findByUsername(username).map(UserEntity::getId);
    }
}
