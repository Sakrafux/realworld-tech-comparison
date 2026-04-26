package com.sakrafux.realworld.infrastructure.adapter.out.persistence.mapper;

import com.sakrafux.realworld.domain.model.Comment;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.entity.CommentEntity;
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
