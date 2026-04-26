package com.sakrafux.realworld.infrastructure.adapter.in.web.mapper;

import com.sakrafux.realworld.domain.model.Tag;
import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.response.TagsResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TagWebMapperTest {

    private final TagWebMapper mapper = Mappers.getMapper(TagWebMapper.class);

    @Test
    void toResponse_validList_returnsResponse() {
        // Given
        List<Tag> tags = List.of(new Tag("tag1"), new Tag("tag2"));

        // When
        TagsResponse response = mapper.toResponse(tags);

        // Then
        assertThat(response.getTags()).containsExactlyInAnyOrder("tag1", "tag2");
    }
}
