package com.sakrafux.realworld.application.port.in.article;

import com.sakrafux.realworld.domain.model.Article;
import lombok.Builder;

import java.util.List;

public interface CreateArticleUseCase {
    Article createArticle(CreateArticleCommand command);

    @Builder
    record CreateArticleCommand(String title, String description, String body, List<String> tagList, String authorEmail) {}
}
