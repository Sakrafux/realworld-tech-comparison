package com.sakrafux.realworld.domain.model;

import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Core business entity representing a User.
 *
 * <p>Note: The social graph (following/followers) is intentionally kept separate from
 * the core identity fields. To prevent infinite recursion and performance issues during
 * mapping and persistence, these collections are usually ignored by general-purpose mappers.
 * Social relationships should be managed through specialized social use cases.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private Long id;
    private String username;
    private String email;
    private String password;
    private String bio;
    private String image;

    @Builder.Default
    private Set<User> following = new HashSet<>();

    @Builder.Default
    private Set<User> followers = new HashSet<>();


    public void update(String email, String username, String password, String bio, String image) {
        if (email != null) {
            this.email = email;
        }
        if (username != null) {
            this.username = username;
        }
        if (password != null) {
            this.password = password;
        }
        if (bio != null) {
            this.bio = bio;
        }
        if (image != null) {
            this.image = image;
        }
    }

}
