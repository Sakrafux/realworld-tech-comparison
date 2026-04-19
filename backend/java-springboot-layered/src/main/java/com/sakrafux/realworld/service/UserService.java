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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public UserResponse registerUser(com.sakrafux.realworld.dto.request.NewUserRequest request) {
        var userData = request.getUser();

        if (userRepository.findByEmail(userData.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists");
        }
        if (userRepository.findByUsername(userData.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        UserEntity user = UserEntity.builder()
                .username(userData.getUsername())
                .email(userData.getEmail())
                .password(passwordEncoder.encode(userData.getPassword()))
                .bio("")
                .build();

        user = userRepository.save(user);
        return toUserResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse loginUser(com.sakrafux.realworld.dto.request.LoginUserRequest request) {
        var userData = request.getUser();

        UserEntity user = userRepository.findByEmail(userData.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userData.getEmail()));

        if (!passwordEncoder.matches(userData.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return toUserResponse(user);
    }


    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        
        return toUserResponse(user);
    }

    private UserResponse toUserResponse(UserEntity user) {
        UserResponse response = userMapper.toResponse(user);
        String token = jwtService.generateToken(user.getEmail());
        response.getUser().setToken(token);
        return response;
    }
}
