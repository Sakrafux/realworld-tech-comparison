package com.sakrafux.realworld.features.comment;

import com.sakrafux.realworld.features.comment.dto.CommentResponse;
import com.sakrafux.realworld.features.comment.dto.MultipleCommentsResponse;
import com.sakrafux.realworld.features.user.dto.ProfileResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "jakarta")
public interface CommentMapper {

    default CommentResponse toResponse(CommentEntity comment, ProfileResponse.ProfileData author) {
        if (comment == null) {
            return null;
        }
        return CommentResponse.builder()
                .comment(toCommentData(comment, author))
                .build();
    }

    @Mapping(target = "author", source = "author")
    @Mapping(target = "createdAt", source = "comment.createdAt")
    @Mapping(target = "updatedAt", source = "comment.updatedAt")
    CommentResponse.CommentData toCommentData(CommentEntity comment, ProfileResponse.ProfileData author);

    default MultipleCommentsResponse toMultipleResponse(List<CommentResponse.CommentData> comments) {
        return MultipleCommentsResponse.builder()
                .comments(comments)
                .build();
    }
}
