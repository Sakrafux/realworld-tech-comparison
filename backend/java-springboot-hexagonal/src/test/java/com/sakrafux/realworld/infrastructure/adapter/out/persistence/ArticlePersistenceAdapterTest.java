package com.sakrafux.realworld.infrastructure.adapter.out.persistence;

import com.sakrafux.realworld.domain.model.Article;
import com.sakrafux.realworld.domain.model.Profile;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.entity.ArticleEntity;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.mapper.ArticlePersistenceMapper;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.repository.ArticleJpaRepository;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.repository.TagJpaRepository;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.repository.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ArticlePersistenceAdapterTest {

    @Mock
    private ArticleJpaRepository articleJpaRepository;
    @Mock
    private UserJpaRepository userJpaRepository;
    @Mock
    private TagJpaRepository tagJpaRepository;
    @Mock
    private ArticlePersistenceMapper articleMapper;

    @InjectMocks
    private ArticlePersistenceAdapter articlePersistenceAdapter;

    @Test
    void save_newArticle_mapsAndSaves() {
        // Given
        Article article = Article.builder()
                .title("Title")
                .author(Profile.builder().username("author").build())
                .tagList(java.util.List.of("tag1"))
                .build();
        ArticleEntity entity = new ArticleEntity();
        UserEntity authorEntity = UserEntity.builder().username("author").build();

        given(articleMapper.toEntity(article)).willReturn(entity);
        given(tagJpaRepository.findByTagIn(any())).willReturn(Set.of());
        given(userJpaRepository.findByUsername("author")).willReturn(Optional.of(authorEntity));
        given(articleJpaRepository.save(entity)).willReturn(entity);
        given(articleMapper.toDomain(entity)).willReturn(article);

        // When
        Article result = articlePersistenceAdapter.save(article);

        // Then
        assertThat(result).isNotNull();
        verify(articleJpaRepository).save(entity);
    }

    @Test
    void findBySlug_existingSlug_returnsArticle() {
        // Given
        ArticleEntity entity = ArticleEntity.builder().slug("slug").build();
        Article article = Article.builder().slug("slug").build();
        given(articleJpaRepository.findBySlug("slug")).willReturn(Optional.of(entity));
        given(articleMapper.toDomain(entity)).willReturn(article);

        // When
        Optional<Article> result = articlePersistenceAdapter.findBySlug("slug");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getSlug()).isEqualTo("slug");
    }

    @Test
    void delete_existingSlug_callsRepository() {
        // Given
        ArticleEntity entity = ArticleEntity.builder().slug("slug").build();
        given(articleJpaRepository.findBySlug("slug")).willReturn(Optional.of(entity));

        // When
        articlePersistenceAdapter.delete("slug");

        // Then
        verify(articleJpaRepository).delete(entity);
    }
}
