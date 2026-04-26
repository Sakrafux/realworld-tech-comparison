package com.sakrafux.realworld.application.service;

import com.sakrafux.realworld.application.port.out.FollowRelationshipPort;
import com.sakrafux.realworld.application.port.out.UserRepository;
import com.sakrafux.realworld.domain.model.Profile;
import com.sakrafux.realworld.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FollowRelationshipPort followRelationshipPort;

    @InjectMocks
    private ProfileService profileService;

    @Test
    void getProfile_withObserver_returnsProfileWithFollowingStatus() {
        // Given
        String targetUsername = "target";
        String observerEmail = "observer@example.com";
        User targetUser = User.builder().id(2L).username(targetUsername).bio("bio").build();
        User observerUser = User.builder().id(1L).email(observerEmail).build();

        given(userRepository.findByUsername(targetUsername)).willReturn(Optional.of(targetUser));
        given(userRepository.findByEmail(observerEmail)).willReturn(Optional.of(observerUser));
        given(followRelationshipPort.isFollowing(1L, 2L)).willReturn(true);

        // When
        Profile result = profileService.getProfile(targetUsername, Optional.of(observerEmail));

        // Then
        assertThat(result.getUsername()).isEqualTo(targetUsername);
        assertThat(result.isFollowing()).isTrue();
    }

    @Test
    void follow_validUser_callsPortAndReturnsProfile() {
        // Given
        String targetUsername = "target";
        String followerEmail = "follower@example.com";
        User targetUser = User.builder().id(2L).username(targetUsername).build();
        User followerUser = User.builder().id(1L).email(followerEmail).build();

        given(userRepository.findByUsername(targetUsername)).willReturn(Optional.of(targetUser));
        given(userRepository.findByEmail(followerEmail)).willReturn(Optional.of(followerUser));

        // When
        Profile result = profileService.follow(targetUsername, followerEmail);

        // Then
        assertThat(result.isFollowing()).isTrue();
        verify(followRelationshipPort).follow(1L, 2L);
    }
}
