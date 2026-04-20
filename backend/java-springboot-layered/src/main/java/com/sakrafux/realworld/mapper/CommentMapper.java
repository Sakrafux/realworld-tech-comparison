package com.sakrafux.realworld.mapper;

import com.sakrafux.realworld.dto.response.CommentResponse;
import com.sakrafux.realworld.dto.response.MultipleCommentsResponse;
import com.sakrafux.realworld.dto.response.ProfileResponse;
import com.sakrafux.realworld.entity.CommentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Mapper for CommentEntity to CommentResponse DTOs.
 */
@Mapper
public interface CommentMapper {

    /**
     * Maps a CommentEntity to CommentResponse.
     */
    default CommentResponse toResponse(CommentEntity comment, ProfileResponse.ProfileData author) {
        return CommentResponse.builder()
                .comment(toCommentData(comment, author))
                .build();
    }

    @Mapping(target = "author", source = "author")
    @Mapping(target = "createdAt", source = "comment.createdAt")
    @Mapping(target = "updatedAt", source = "comment.updatedAt")
    CommentResponse.CommentData toCommentData(CommentEntity comment, ProfileResponse.ProfileData author);

    /**
     * Maps a list of CommentData to MultipleCommentsResponse.
     */
    default MultipleCommentsResponse toMultipleResponse(List<CommentResponse.CommentData> comments) {
        return MultipleCommentsResponse.builder()
                .comments(comments)
                .build();
    }

    /**
     * Utility method for MapStruct to convert Instant to ZonedDateTime.
     */
    default ZonedDateTime map(Instant instant) {
        return instant == null ? null : ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));
    }
}
