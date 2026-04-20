package com.sakrafux.realworld.service;

import com.sakrafux.realworld.dto.request.UpdateUserRequest;
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

/**
 * Service class responsible for handling core user authentication and profile management.
 * Acts as an intermediary between the Controllers (UserController, UsersController) and UserRepository.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Registers a new user in the system after validating that the email and username
     * are not already taken. Generates a fresh JWT token for the newly registered user.
     *
     * @param request the registration details containing email, username, and password
     * @return a UserResponse containing user details and a valid JWT token
     * @throws UserAlreadyExistsException if the provided email or username already exists
     */
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
        String token = jwtService.generateToken(user.getEmail());
        return userMapper.toResponse(user, token);
    }

    /**
     * Authenticates an existing user by verifying their email and password against
     * the stored credentials. Generates a fresh JWT token upon successful authentication.
     *
     * @param request the login details containing email and password
     * @return a UserResponse containing user details and a valid JWT token
     * @throws ResourceNotFoundException if no user is found with the provided email
     * @throws InvalidCredentialsException if the provided password does not match the stored hash
     */
    @Transactional(readOnly = true)
    public UserResponse loginUser(com.sakrafux.realworld.dto.request.LoginUserRequest request) {
        var userData = request.getUser();

        UserEntity user = userRepository.findByEmail(userData.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userData.getEmail()));

        if (!passwordEncoder.matches(userData.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user.getEmail());
        return userMapper.toResponse(user, token);
    }

    /**
     * Retrieves the profile information of the currently authenticated user
     * using their email address.
     *
     * @param email the email of the currently authenticated user
     * @return a UserResponse containing the user's details and a freshly generated JWT token
     * @throws ResourceNotFoundException if the user cannot be found in the database
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        
        String token = jwtService.generateToken(user.getEmail());
        return userMapper.toResponse(user, token);
    }

    /**
     * Updates the profile of the currently authenticated user.
     * All fields are optional; only those provided in the request will be updated.
     *
     * @param currentEmail the email of the currently authenticated user
     * @param request the update details containing new email, username, password, bio, or image
     * @return a UserResponse containing the updated user details and a new JWT token
     * @throws ResourceNotFoundException if the user cannot be found in the database
     * @throws UserAlreadyExistsException if the new email or username is already taken by another user
     */
    @Transactional
    public UserResponse updateUser(String currentEmail, UpdateUserRequest request) {
        UserEntity user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        UpdateUserRequest.UserData userData = request.getUser();

        if (userData.getEmail() != null && !userData.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(userData.getEmail()).isPresent()) {
                throw new UserAlreadyExistsException("Email already exists");
            }
            user.setEmail(userData.getEmail());
        }

        if (userData.getUsername() != null && !userData.getUsername().equals(user.getUsername())) {
            if (userRepository.findByUsername(userData.getUsername()).isPresent()) {
                throw new UserAlreadyExistsException("Username already exists");
            }
            user.setUsername(userData.getUsername());
        }

        if (userData.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userData.getPassword()));
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
}
