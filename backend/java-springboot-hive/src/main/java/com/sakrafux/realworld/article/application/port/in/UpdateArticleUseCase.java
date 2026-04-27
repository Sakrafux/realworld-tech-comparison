package com.sakrafux.realworld.article.application.port.in;

import com.sakrafux.realworld.article.domain.Article;
import lombok.Builder;

public interface UpdateArticleUseCase {
    Article updateArticle(UpdateArticleCommand command);

    @Builder
    record UpdateArticleCommand(String slug, String title, String description, String body, String authorEmail) {}
}
