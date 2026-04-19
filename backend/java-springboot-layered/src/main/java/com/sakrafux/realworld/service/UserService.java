package com.sakrafux.realworld.service;

import com.sakrafux.realworld.dto.response.UserResponse;
import com.sakrafux.realworld.entity.UserEntity;
import com.sakrafux.realworld.mapper.UserMapper;
import com.sakrafux.realworld.exception.ResourceNotFoundException;
import com.sakrafux.realworld.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        
        UserResponse response = userMapper.toResponse(user);
        // TODO: Set actual JWT token here after implementing security
        response.getUser().setToken("dummy-token"); 
        
        return response;
    }
}
