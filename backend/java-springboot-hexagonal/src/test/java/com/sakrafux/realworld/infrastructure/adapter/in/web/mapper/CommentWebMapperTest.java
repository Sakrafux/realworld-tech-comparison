package com.sakrafux.realworld.infrastructure.adapter.in.web.mapper;

import com.sakrafux.realworld.domain.model.Comment;
import com.sakrafux.realworld.domain.model.Profile;
import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.response.CommentResponse;
import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.response.MultipleCommentsResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentWebMapperTest {

    @Spy
    private ProfileWebMapper profileWebMapper = Mappers.getMapper(ProfileWebMapper.class);

    @InjectMocks
    private CommentWebMapperImpl mapper;

    @Test
    void toResponse_validDomain_returnsResponse() {
        // Given
        Comment comment = Comment.builder()
                .id(1L)
                .body("body")
                .author(Profile.builder().username("author").build())
                .build();

        // When
        CommentResponse response = mapper.toResponse(comment);

        // Then
        assertThat(response.getComment().getBody()).isEqualTo("body");
        assertThat(response.getComment().getAuthor().getUsername()).isEqualTo("author");
    }

    @Test
    void toMultipleResponse_validList_returnsResponse() {
        // Given
        Comment comment = Comment.builder()
                .id(1L)
                .body("body")
                .author(Profile.builder().username("author").build())
                .build();
        List<Comment> comments = List.of(comment);

        // When
        MultipleCommentsResponse response = mapper.toMultipleResponse(comments);

        // Then
        assertThat(response.getComments()).hasSize(1);
        assertThat(response.getComments().get(0).getBody()).isEqualTo("body");
    }
}
