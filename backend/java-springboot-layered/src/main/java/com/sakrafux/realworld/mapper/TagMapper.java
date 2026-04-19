package com.sakrafux.realworld.mapper;

import com.sakrafux.realworld.dto.response.TagsResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TagMapper {

    default TagsResponse toResponse(List<String> tags) {
        return TagsResponse.builder()
                .tags(tags)
                .build();
    }
}
