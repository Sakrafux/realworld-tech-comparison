package com.sakrafux.realworld.application.service;

import com.sakrafux.realworld.application.port.out.TagRepository;
import com.sakrafux.realworld.domain.model.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    @Test
    void getTags_returnsAllTags() {
        // Given
        List<Tag> tags = List.of(new Tag("java"), new Tag("spring"));
        given(tagRepository.findAll()).willReturn(tags);

        // When
        List<Tag> result = tagService.getTags();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(new Tag("java"), new Tag("spring"));
    }
}
