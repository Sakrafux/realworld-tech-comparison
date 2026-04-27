package com.sakrafux.realworld.article.application.port.in;

import com.sakrafux.realworld.article.domain.Article;
import lombok.Builder;

import java.util.List;

public interface CreateArticleUseCase {
    Article createArticle(CreateArticleCommand command);

    @Builder
    record CreateArticleCommand(String title, String description, String body, List<String> tagList, String authorEmail) {}
}
