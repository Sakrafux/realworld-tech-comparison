package com.sakrafux.realworld.features.article;

import com.sakrafux.realworld.features.article.dto.TagsResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "jakarta")
public interface TagMapper {

    default TagsResponse toResponse(List<String> tags) {
        if (tags == null) {
            return null;
        }
        return TagsResponse.builder()
                .tags(tags)
                .build();
    }
}
