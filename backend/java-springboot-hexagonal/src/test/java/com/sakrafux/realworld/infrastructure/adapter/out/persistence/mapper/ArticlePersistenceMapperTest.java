package com.sakrafux.realworld.infrastructure.adapter.out.persistence.mapper;

import com.sakrafux.realworld.domain.model.Article;
import com.sakrafux.realworld.domain.model.Profile;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.entity.ArticleEntity;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.entity.TagEntity;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArticlePersistenceMapperTest {

    @Spy
    private UserPersistenceMapper userPersistenceMapper = Mappers.getMapper(UserPersistenceMapper.class);

    @InjectMocks
    private ArticlePersistenceMapperImpl mapper;

    @Test
    void toDomain_validEntity_returnsDomain() {
        // Given
        UserEntity authorEntity = UserEntity.builder().username("author").build();
        TagEntity tagEntity = TagEntity.builder().tag("tag1").build();
        ArticleEntity entity = ArticleEntity.builder()
                .title("Title")
                .slug("slug")
                .author(authorEntity)
                .tags(new HashSet<>(Set.of(tagEntity)))
                .favoritedBy(new HashSet<>())
                .build();

        // When
        Article domain = mapper.toDomain(entity);

        // Then
        assertThat(domain.getTitle()).isEqualTo("Title");
        assertThat(domain.getAuthor().getUsername()).isEqualTo("author");
        assertThat(domain.getTagList()).containsExactly("tag1");
    }

    @Test
    void toEntity_validDomain_returnsEntity() {
        // Given
        Article domain = Article.builder()
                .title("Title")
                .author(Profile.builder().username("author").build())
                .build();

        // When
        ArticleEntity entity = mapper.toEntity(domain);

        // Then
        assertThat(entity.getTitle()).isEqualTo("Title");
        assertThat(entity.getAuthor().getUsername()).isEqualTo("author");
    }
}
