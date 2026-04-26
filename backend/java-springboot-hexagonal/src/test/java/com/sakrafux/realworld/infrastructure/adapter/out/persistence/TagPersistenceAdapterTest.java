package com.sakrafux.realworld.infrastructure.adapter.out.persistence;

import com.sakrafux.realworld.domain.model.Tag;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.entity.TagEntity;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.repository.TagJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TagPersistenceAdapterTest {

    @Mock
    private TagJpaRepository tagJpaRepository;

    @InjectMocks
    private TagPersistenceAdapter tagPersistenceAdapter;

    @Test
    void findAll_returnsDomainTags() {
        // Given
        given(tagJpaRepository.findAllTagNames()).willReturn(List.of("tag1", "tag2"));

        // When
        List<Tag> result = tagPersistenceAdapter.findAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("tag1");
        assertThat(result.get(1).getName()).isEqualTo("tag2");
    }

    @Test
    void findByNames_returnsMatchingTags() {
        // Given
        List<String> names = List.of("tag1");
        TagEntity entity = TagEntity.builder().tag("tag1").build();
        given(tagJpaRepository.findByTagIn(names)).willReturn(Set.of(entity));

        // When
        List<Tag> result = tagPersistenceAdapter.findByNames(names);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("tag1");
    }

    @Test
    void saveAll_callsRepository() {
        // Given
        List<Tag> tags = List.of(new Tag("tag1"));

        // When
        tagPersistenceAdapter.saveAll(tags);

        // Then
        verify(tagJpaRepository).saveAll(any());
    }
}
