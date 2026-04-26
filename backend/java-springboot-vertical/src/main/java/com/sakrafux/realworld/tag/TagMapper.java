package com.sakrafux.realworld.tag;

import com.sakrafux.realworld.tag.response.TagsResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface TagMapper {

    default TagsResponse toResponse(List<String> tags) {
        return TagsResponse.builder()
                .tags(tags)
                .build();
    }
}
