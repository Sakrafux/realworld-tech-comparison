package com.sakrafux.realworld.article.infrastructure.persistence.mapper;

import com.sakrafux.realworld.article.domain.Author;
import com.sakrafux.realworld.article.domain.Comment;
import com.sakrafux.realworld.article.infrastructure.persistence.entity.CommentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Mapper
public interface CommentPersistenceMapper {

    @Mapping(target = "author", source = "authorId")
    Comment toDomain(CommentEntity entity);

    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "article", ignore = true)
    CommentEntity toEntity(Comment domain);

    default Author mapAuthorId(Long authorId) {
        if (authorId == null) return null;
        return Author.builder().username("").build(); // Placeholder, service hydrates this
    }

    default Long mapAuthorToId(Author author) {
        return null; // Not used for mapping to entity since we handle it in adapter
    }

    default ZonedDateTime map(Instant instant) {
        return instant == null ? null : ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));
    }
}
