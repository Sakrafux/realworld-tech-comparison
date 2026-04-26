package com.sakrafux.realworld.application.port.in.article;

public interface GetFeedQuery {
    ArticleListResult getFeed(int limit, int offset, String observerEmail);
}
