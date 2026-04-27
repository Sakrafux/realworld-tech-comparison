package com.sakrafux.realworld.article.application.port.in;

import com.sakrafux.realworld.article.domain.Article;
import java.util.List;

public record ArticleListResult(List<Article> articles, long totalCount) {}
