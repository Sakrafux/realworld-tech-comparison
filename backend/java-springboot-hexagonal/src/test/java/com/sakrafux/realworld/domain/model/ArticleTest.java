package com.sakrafux.realworld.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleTest {

    @Test
    void update_allFieldsProvided_updatesCorrectly() {
        // Given
        Article article = Article.builder()
                .title("Old Title")
                .slug("old-title")
                .description("Old Desc")
                .body("Old Body")
                .build();

        // When
        article.update("New Title", "new-title", "New Desc", "New Body");

        // Then
        assertThat(article.getTitle()).isEqualTo("New Title");
        assertThat(article.getSlug()).isEqualTo("new-title");
        assertThat(article.getDescription()).isEqualTo("New Desc");
        assertThat(article.getBody()).isEqualTo("New Body");
    }

    @Test
    void update_someFieldsNull_onlyUpdatesNonNullFields() {
        // Given
        Article article = Article.builder()
                .title("Old Title")
                .slug("old-title")
                .description("Old Desc")
                .body("Old Body")
                .build();

        // When
        article.update(null, "new-slug", null, "New Body");

        // Then
        assertThat(article.getTitle()).isEqualTo("Old Title");
        assertThat(article.getSlug()).isEqualTo("new-slug");
        assertThat(article.getDescription()).isEqualTo("Old Desc");
        assertThat(article.getBody()).isEqualTo("New Body");
    }
}
