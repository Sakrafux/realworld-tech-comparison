package com.sakrafux.realworld.features.article;

import com.sakrafux.realworld.features.article.dto.TagsResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface TagMapper {

    default TagsResponse toResponse(List<String> tags) {
        return TagsResponse.builder()
                .tags(tags)
                .build();
    }
}
