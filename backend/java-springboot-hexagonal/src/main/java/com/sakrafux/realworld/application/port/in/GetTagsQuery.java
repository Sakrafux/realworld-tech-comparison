package com.sakrafux.realworld.application.port.in;

import com.sakrafux.realworld.domain.model.Tag;

import java.util.List;

public interface GetTagsQuery {
    List<Tag> getTags();
}
