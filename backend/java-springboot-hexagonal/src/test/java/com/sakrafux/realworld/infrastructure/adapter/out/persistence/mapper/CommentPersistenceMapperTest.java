package com.sakrafux.realworld.infrastructure.adapter.out.persistence.mapper;

import com.sakrafux.realworld.domain.model.Profile;
import com.sakrafux.realworld.domain.model.Comment;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.entity.CommentEntity;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentPersistenceMapperTest {

    @Spy
    private UserPersistenceMapper userPersistenceMapper = Mappers.getMapper(UserPersistenceMapper.class);

    @InjectMocks
    private CommentPersistenceMapperImpl mapper;

    @Test
    void toDomain_validEntity_returnsDomain() {
        // Given
        UserEntity authorEntity = UserEntity.builder().username("author").build();
        CommentEntity entity = CommentEntity.builder()
                .id(1L)
                .body("body")
                .author(authorEntity)
                .build();

        // When
        Comment domain = mapper.toDomain(entity);

        // Then
        assertThat(domain.getId()).isEqualTo(1L);
        assertThat(domain.getBody()).isEqualTo("body");
        assertThat(domain.getAuthor().getUsername()).isEqualTo("author");
    }

    @Test
    void toEntity_validDomain_returnsEntity() {
        // Given
        Comment domain = Comment.builder()
                .body("body")
                .author(Profile.builder().username("author").build())
                .build();

        // When
        CommentEntity entity = mapper.toEntity(domain);

        // Then
        assertThat(entity.getBody()).isEqualTo("body");
        assertThat(entity.getAuthor().getUsername()).isEqualTo("author");
    }
}
