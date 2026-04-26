package com.sakrafux.realworld.infrastructure.adapter.in.web.mapper;

import com.sakrafux.realworld.domain.model.Comment;
import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.response.CommentResponse;
import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.response.MultipleCommentsResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(uses = {ProfileWebMapper.class})
public interface CommentWebMapper {

    @Mapping(target = "comment", source = "domain")
    CommentResponse toResponse(Comment domain);

    default MultipleCommentsResponse toMultipleResponse(List<Comment> comments) {
        return MultipleCommentsResponse.builder()
                .comments(comments.stream().map(this::toCommentData).toList())
                .build();
    }

    CommentResponse.CommentData toCommentData(Comment domain);
}
