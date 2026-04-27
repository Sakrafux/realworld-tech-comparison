package com.sakrafux.realworld.article.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Value object representing an Author within the Article cell.
 * This decouples the Article cell from the User cell's Profile domain model.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Author {
    private String username;
    private String bio;
    private String image;
    private boolean following;
}
