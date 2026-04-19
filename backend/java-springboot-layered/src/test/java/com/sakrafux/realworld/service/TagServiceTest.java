package com.sakrafux.realworld.service;

import com.sakrafux.realworld.dto.response.TagsResponse;
import com.sakrafux.realworld.mapper.TagMapper;
import com.sakrafux.realworld.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TagMapper tagMapper;

    @InjectMocks
    private TagService tagService;

    @Test
    void getAllTags_TagsExist_ReturnsTagsResponse() {
        // Given
        List<String> tags = List.of("reactjs", "angularjs");
        TagsResponse expectedResponse = TagsResponse.builder().tags(tags).build();
        
        given(tagRepository.findAllTagNames()).willReturn(tags);
        given(tagMapper.toResponse(tags)).willReturn(expectedResponse);

        // When
        TagsResponse result = tagService.getAllTags();

        // Then
        assertThat(result).isEqualTo(expectedResponse);
        verify(tagRepository).findAllTagNames();
        verify(tagMapper).toResponse(tags);
    }
}
