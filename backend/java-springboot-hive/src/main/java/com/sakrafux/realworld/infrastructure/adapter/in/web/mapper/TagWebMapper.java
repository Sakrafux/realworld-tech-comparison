package com.sakrafux.realworld.infrastructure.adapter.in.web.mapper;

import com.sakrafux.realworld.domain.model.Tag;
import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.response.TagsResponse;
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
