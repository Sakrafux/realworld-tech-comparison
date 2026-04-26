package com.sakrafux.realworld.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void update_allFieldsProvided_updatesAllFields() {
        // Given
        User user = User.builder()
                .email("old@example.com")
                .username("oldUser")
                .password("oldPass")
                .bio("old bio")
                .image("old image")
                .build();

        // When
        user.update("new@example.com", "newUser", "newPass", "new bio", "new image");

        // Then
        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.getUsername()).isEqualTo("newUser");
        assertThat(user.getPassword()).isEqualTo("newPass");
        assertThat(user.getBio()).isEqualTo("new bio");
        assertThat(user.getImage()).isEqualTo("new image");
    }

    @Test
    void update_someFieldsNull_onlyUpdatesProvidedFields() {
        // Given
        User user = User.builder()
                .email("old@example.com")
                .username("oldUser")
                .password("oldPass")
                .build();

        // When
        user.update(null, "newUser", null, "new bio", null);

        // Then
        assertThat(user.getEmail()).isEqualTo("old@example.com");
        assertThat(user.getUsername()).isEqualTo("newUser");
        assertThat(user.getPassword()).isEqualTo("oldPass");
        assertThat(user.getBio()).isEqualTo("new bio");
    }
}
