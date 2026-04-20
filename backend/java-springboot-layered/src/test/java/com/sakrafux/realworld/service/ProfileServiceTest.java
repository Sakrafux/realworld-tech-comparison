package com.sakrafux.realworld.service;

import com.sakrafux.realworld.dto.response.ProfileResponse;
import com.sakrafux.realworld.entity.UserEntity;
import com.sakrafux.realworld.exception.ResourceNotFoundException;
import com.sakrafux.realworld.mapper.ProfileMapper;
import com.sakrafux.realworld.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Spy
    private ProfileMapper profileMapper = Mappers.getMapper(ProfileMapper.class);

    @InjectMocks
    private ProfileService profileService;

    @Test
    void getProfile_UserExistsAndNotAuthenticated_ReturnsProfile() {
        // Given
        String targetUsername = "targetUser";
        UserEntity targetUser = UserEntity.builder()
                .username(targetUsername)
                .bio("target bio")
                .image("target image")
                .build();

        given(userRepository.findByUsername(targetUsername)).willReturn(Optional.of(targetUser));

        // When
        ProfileResponse result = profileService.getProfile(targetUsername, Optional.empty());

        // Then
        assertThat(result.getProfile().getUsername()).isEqualTo(targetUsername);
        assertThat(result.getProfile().isFollowing()).isFalse();
    }

    @Test
    void getProfile_UserExistsAndFollowing_ReturnsProfile() {
        // Given
        String targetUsername = "targetUser";
        String currentUserEmail = "current@example.com";
        UserEntity targetUser = UserEntity.builder().username(targetUsername).build();
        UserEntity currentUser = UserEntity.builder()
                .email(currentUserEmail)
                .following(new HashSet<>())
                .build();
        currentUser.getFollowing().add(targetUser);

        given(userRepository.findByUsername(targetUsername)).willReturn(Optional.of(targetUser));
        given(userRepository.findByEmail(currentUserEmail)).willReturn(Optional.of(currentUser));

        // When
        ProfileResponse result = profileService.getProfile(targetUsername, Optional.of(currentUserEmail));

        // Then
        assertThat(result.getProfile().getUsername()).isEqualTo(targetUsername);
        assertThat(result.getProfile().isFollowing()).isTrue();
    }

    @Test
    void getProfile_UserNotFound_ThrowsResourceNotFoundException() {
        // Given
        String targetUsername = "nonexistent";
        given(userRepository.findByUsername(targetUsername)).willReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> profileService.getProfile(targetUsername, Optional.empty()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void followUser_ValidUser_AddsFollowerAndReturnsProfile() {
        // Given
        String targetUsername = "targetUser";
        String currentUserEmail = "current@example.com";
        UserEntity targetUser = UserEntity.builder().username(targetUsername).build();
        UserEntity currentUser = UserEntity.builder()
                .email(currentUserEmail)
                .following(new HashSet<>())
                .build();

        given(userRepository.findByUsername(targetUsername)).willReturn(Optional.of(targetUser));
        given(userRepository.findByEmail(currentUserEmail)).willReturn(Optional.of(currentUser));
        given(userRepository.save(currentUser)).willAnswer(invocation -> invocation.getArgument(0));

        // When
        ProfileResponse result = profileService.followUser(targetUsername, currentUserEmail);

        // Then
        assertThat(result.getProfile().getUsername()).isEqualTo(targetUsername);
        assertThat(result.getProfile().isFollowing()).isTrue();
        assertThat(currentUser.getFollowing()).contains(targetUser);
        verify(userRepository).save(currentUser);
    }

    @Test
    void unfollowUser_ValidUser_RemovesFollowerAndReturnsProfile() {
        // Given
        String targetUsername = "targetUser";
        String currentUserEmail = "current@example.com";
        UserEntity targetUser = UserEntity.builder().username(targetUsername).build();
        UserEntity currentUser = UserEntity.builder()
                .email(currentUserEmail)
                .following(new HashSet<>())
                .build();
        currentUser.getFollowing().add(targetUser);

        given(userRepository.findByUsername(targetUsername)).willReturn(Optional.of(targetUser));
        given(userRepository.findByEmail(currentUserEmail)).willReturn(Optional.of(currentUser));
        given(userRepository.save(currentUser)).willAnswer(invocation -> invocation.getArgument(0));

        // When
        ProfileResponse result = profileService.unfollowUser(targetUsername, currentUserEmail);

        // Then
        assertThat(result.getProfile().getUsername()).isEqualTo(targetUsername);
        assertThat(result.getProfile().isFollowing()).isFalse();
        assertThat(currentUser.getFollowing()).doesNotContain(targetUser);
        verify(userRepository).save(currentUser);
    }
}
