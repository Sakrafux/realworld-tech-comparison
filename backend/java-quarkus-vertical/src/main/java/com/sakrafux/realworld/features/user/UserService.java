package com.sakrafux.realworld.features.user;

import com.sakrafux.realworld.core.exception.InvalidCredentialsException;
import com.sakrafux.realworld.core.exception.ResourceAlreadyExistsException;
import com.sakrafux.realworld.core.exception.ResourceNotFoundException;
import com.sakrafux.realworld.features.user.dto.LoginUserRequest;
import com.sakrafux.realworld.features.user.dto.NewUserRequest;
import com.sakrafux.realworld.features.user.dto.UpdateUserRequest;
import com.sakrafux.realworld.features.user.dto.UserResponse;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @Transactional
    public UserResponse registerUser(NewUserRequest request) {
        var userData = request.getUser();

        if (UserEntity.findByEmail(userData.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }
        if (UserEntity.findByUsername(userData.getUsername()).isPresent()) {
            throw new ResourceAlreadyExistsException("Username already exists");
        }

        UserEntity user = UserEntity.builder()
                .username(userData.getUsername())
                .email(userData.getEmail())
                .password(BcryptUtil.bcryptHash(userData.getPassword()))
                .bio("")
                .build();

        user.persist();
        
        String token = generateToken(user.getEmail());
        return userMapper.toResponse(user, token);
    }

    public UserResponse loginUser(LoginUserRequest request) {
        var userData = request.getUser();

        UserEntity user = UserEntity.findByEmail(userData.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userData.getEmail()));

        if (!BcryptUtil.matches(userData.getPassword(), user.password)) {
            throw new InvalidCredentialsException();
        }

        String token = generateToken(user.getEmail());
        return userMapper.toResponse(user, token);
    }

    public UserResponse getCurrentUser(String email) {
        UserEntity user = UserEntity.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        String token = generateToken(user.getEmail());
        return userMapper.toResponse(user, token);
    }

    @Transactional
    public UserResponse updateUser(String currentEmail, UpdateUserRequest request) {
        UserEntity user = UserEntity.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        UpdateUserRequest.UserData userData = request.getUser();

        if (userData.getEmail() != null && !userData.getEmail().equals(user.email)) {
            if (UserEntity.findByEmail(userData.getEmail()).isPresent()) {
                throw new ResourceAlreadyExistsException("Email already exists");
            }
            user.email = userData.getEmail();
        }

        if (userData.getUsername() != null && !userData.getUsername().equals(user.username)) {
            if (UserEntity.findByUsername(userData.getUsername()).isPresent()) {
                throw new ResourceAlreadyExistsException("Username already exists");
            }
            user.username = userData.getUsername();
        }

        if (userData.getPassword() != null) {
            user.password = BcryptUtil.bcryptHash(userData.getPassword());
        }

        if (userData.getBio() != null) {
            user.bio = userData.getBio();
        }

        if (userData.getImage() != null) {
            user.image = userData.getImage();
        }

        String token = generateToken(user.email);
        return userMapper.toResponse(user, token);
    }

    private String generateToken(String email) {
        return Jwt.issuer(issuer)
                .upn(email)
                .sign();
    }
}
