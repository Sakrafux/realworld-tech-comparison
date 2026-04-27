package com.sakrafux.realworld.article.application.port.out;

import com.sakrafux.realworld.article.domain.Tag;

import java.util.List;

public interface TagRepository {
    List<Tag> findAll();
    List<Tag> findByNames(List<String> names);
    void saveAll(List<Tag> tags);
}
