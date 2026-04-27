package com.sakrafux.realworld.article.infrastructure.web.mapper;

import com.sakrafux.realworld.article.domain.Tag;
import com.sakrafux.realworld.article.infrastructure.web.dto.response.TagsResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface TagWebMapper {

    default TagsResponse toResponse(List<Tag> tags) {
        return TagsResponse.builder()
                .tags(tags.stream().map(Tag::getName).toList())
                .build();
    }
}
