package com.sakrafux.realworld.service;

import com.sakrafux.realworld.dto.response.TagsResponse;
import com.sakrafux.realworld.mapper.TagMapper;
import com.sakrafux.realworld.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Spy
    private TagMapper tagMapper = Mappers.getMapper(TagMapper.class);

    @InjectMocks
    private TagService tagService;

    @Test
    void getAllTags_TagsExist_ReturnsTagsResponse() {
        // Given
        List<String> tags = List.of("reactjs", "angularjs");
        
        given(tagRepository.findAllTagNames()).willReturn(tags);

        // When
        TagsResponse result = tagService.getAllTags();

        // Then
        assertThat(result.getTags()).containsExactlyInAnyOrder("reactjs", "angularjs");
        verify(tagRepository).findAllTagNames();
    }
}
