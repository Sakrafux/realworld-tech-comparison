package com.sakrafux.realworld.infrastructure.adapter.in.web.dto.response;

import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.response.CommentResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultipleCommentsResponse {
    private List<CommentResponse.CommentData> comments;
}
