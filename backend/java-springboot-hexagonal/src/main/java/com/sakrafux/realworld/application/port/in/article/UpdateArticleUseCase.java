package com.sakrafux.realworld.application.port.in.article;

import com.sakrafux.realworld.domain.model.Article;
import lombok.Builder;

public interface UpdateArticleUseCase {
    Article updateArticle(UpdateArticleCommand command);

    @Builder
    record UpdateArticleCommand(String slug, String title, String description, String body, String authorEmail) {}
}
