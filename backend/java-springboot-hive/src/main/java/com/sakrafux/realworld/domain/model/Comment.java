package com.sakrafux.realworld.domain.model;

import com.sakrafux.realworld.domain.model.Profile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * Domain model representing a Comment on an Article.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {
    private Long id;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private String body;
    private Profile author;
}
