package com.sakrafux.realworld.application.port.in.article;

import com.sakrafux.realworld.domain.model.Article;
import java.util.List;

public record ArticleListResult(List<Article> articles, long totalCount) {}
