package com.sakrafux.realworld.controller;

import com.sakrafux.realworld.dto.request.NewCommentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link ArticlesCommentsController}.
 */
class ArticlesCommentsControllerIT extends AbstractControllerIT {

    @Test
    void addComment_ValidRequest_ReturnsOkWithComment() throws Exception {
        String token = registerUserViaApi("author", "author@example.com", "password123");
        createArticleViaApi(token, "Article", "Desc", "Body", List.of());

        NewCommentRequest request = NewCommentRequest.builder()
                .comment(NewCommentRequest.CommentData.builder().body("Test Comment").build())
                .build();

        mockMvc.perform(post("/articles/article/comments")
                        .header("Authorization", "Token " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment.body").value("Test Comment"))
                .andExpect(jsonPath("$.comment.author.username").value("author"));
    }

    @Test
    void getComments_ArticleExists_ReturnsOkWithComments() throws Exception {
        String token = registerUserViaApi("author", "author@example.com", "password123");
        createArticleViaApi(token, "Article", "Desc", "Body", List.of());

        NewCommentRequest request = NewCommentRequest.builder()
                .comment(NewCommentRequest.CommentData.builder().body("Test Comment").build())
                .build();

        mockMvc.perform(post("/articles/article/comments")
                        .header("Authorization", "Token " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/articles/article/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments", hasSize(1)))
                .andExpect(jsonPath("$.comments[0].body").value("Test Comment"));
    }

    @Test
    void deleteComment_AuthorDeletes_ReturnsOk() throws Exception {
        String token = registerUserViaApi("author", "author@example.com", "password123");
        createArticleViaApi(token, "Article", "Desc", "Body", List.of());

        NewCommentRequest request = NewCommentRequest.builder()
                .comment(NewCommentRequest.CommentData.builder().body("Test Comment").build())
                .build();

        String response = mockMvc.perform(post("/articles/article/comments")
                        .header("Authorization", "Token " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long commentId = objectMapper.readTree(response).get("comment").get("id").asLong();

        mockMvc.perform(delete("/articles/article/comments/" + commentId)
                        .header("Authorization", "Token " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/articles/article/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments", hasSize(0)));
    }
}
