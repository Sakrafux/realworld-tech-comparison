package com.sakrafux.realworld.article.application.port.in;

public interface GetFeedQuery {
    ArticleListResult getFeed(int limit, int offset, String observerEmail);
}
