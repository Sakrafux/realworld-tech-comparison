package com.sakrafux.realworld.infrastructure.adapter.out.persistence;

import com.sakrafux.realworld.domain.model.Tag;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.repository.TagJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

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
}
