package com.sakrafux.realworld.article.domain;

import com.sakrafux.realworld.user.domain.Profile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Domain model representing an Article.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Article {
    private Long id;
    private String slug;
    private String title;
    private String description;
    private String body;
    private List<String> tagList;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private boolean favorited;
    private int favoritesCount;
    private Profile author;

    public void update(String title, String slug, String description, String body) {
        if (title != null) {
            this.title = title;
        }
        if (slug != null) {
            this.slug = slug;
        }
        if (description != null) {
            this.description = description;
        }
        if (body != null) {
            this.body = body;
        }
    }
}
