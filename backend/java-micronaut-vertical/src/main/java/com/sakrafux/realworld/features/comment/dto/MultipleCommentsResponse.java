package com.sakrafux.realworld.features.comment.dto;

import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Serdeable
public class MultipleCommentsResponse {
    private List<CommentResponse.CommentData> comments;
}
