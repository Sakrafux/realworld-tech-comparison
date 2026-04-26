package com.sakrafux.realworld.application.port.out;

import com.sakrafux.realworld.domain.model.Tag;

import java.util.List;

public interface TagRepository {
    List<Tag> findAll();
}
