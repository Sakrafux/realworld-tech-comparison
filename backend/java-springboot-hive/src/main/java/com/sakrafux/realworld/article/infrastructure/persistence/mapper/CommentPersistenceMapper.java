package com.sakrafux.realworld.article.infrastructure.persistence.mapper;

import com.sakrafux.realworld.article.domain.Comment;
import com.sakrafux.realworld.article.infrastructure.persistence.entity.CommentEntity;
import com.sakrafux.realworld.user.infrastructure.persistence.mapper.UserPersistenceMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Mapper(uses = {UserPersistenceMapper.class})
public interface CommentPersistenceMapper {

    @Mapping(target = "author", source = "author")
    @Mapping(target = "author.following", ignore = true)
    Comment toDomain(CommentEntity entity);

    @Mapping(target = "author", source = "author")
    @Mapping(target = "author.following", ignore = true)
    @Mapping(target = "article", ignore = true)
    CommentEntity toEntity(Comment domain);

    default ZonedDateTime map(Instant instant) {
        return instant == null ? null : ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));
    }
}
