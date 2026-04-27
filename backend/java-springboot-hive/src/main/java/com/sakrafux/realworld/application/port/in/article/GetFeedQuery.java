package com.sakrafux.realworld.application.port.in.article;

import com.sakrafux.realworld.application.port.in.article.ArticleListResult;

public interface GetFeedQuery {
    ArticleListResult getFeed(int limit, int offset, String observerEmail);
}
