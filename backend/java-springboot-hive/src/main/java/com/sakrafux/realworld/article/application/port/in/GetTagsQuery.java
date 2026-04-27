package com.sakrafux.realworld.article.application.port.in;

import com.sakrafux.realworld.article.domain.Tag;

import java.util.List;

public interface GetTagsQuery {
    List<Tag> getTags();
}
