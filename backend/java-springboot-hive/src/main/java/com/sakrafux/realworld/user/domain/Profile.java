package com.sakrafux.realworld.user.domain;

import lombok.Builder;
import lombok.Value;

/**
 * Domain model representing a public user profile.
 * This is a "view" of a User relative to an observer.
 */
@Value
@Builder
public class Profile {
    String username;
    String bio;
    String image;
    boolean following;
}
