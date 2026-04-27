package com.sakrafux.realworld.article.infrastructure.persistence.mapper;

import com.sakrafux.realworld.article.domain.Article;
import com.sakrafux.realworld.article.domain.Author;
import com.sakrafux.realworld.article.infrastructure.persistence.entity.ArticleEntity;
import com.sakrafux.realworld.article.infrastructure.persistence.entity.TagEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper
public interface ArticlePersistenceMapper {

    @Mapping(target = "tagList", source = "tags")
    @Mapping(target = "favorited", ignore = true)
    @Mapping(target = "favoritesCount", expression = "java(entity.getFavoritedByUserIds() != null ? entity.getFavoritedByUserIds().size() : 0)")
    @Mapping(target = "author", source = "authorId")
    Article toDomain(ArticleEntity entity);

    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "favoritedByUserIds", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "authorId", source = "author.id")
    ArticleEntity toEntity(Article domain);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "favoritedByUserIds", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    void updateEntityFromDomain(Article domain, @MappingTarget ArticleEntity entity);

    default Author mapAuthorId(Long authorId) {
        if (authorId == null) return null;
        return Author.builder().username("").build(); // Placeholder, service hydrates this
    }

    default Long mapAuthorToId(Author author) {
        return null; // Not used for mapping to entity since we handle it in adapter
    }

    default List<String> mapTags(Set<TagEntity> tags) {
        if (tags == null) return Collections.emptyList();
        return tags.stream()
                .map(TagEntity::getTag)
                .sorted()
                .collect(Collectors.toList());
    }

    default ZonedDateTime map(Instant instant) {
        return instant == null ? null : ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));
    }
}
