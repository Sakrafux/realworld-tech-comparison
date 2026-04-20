package com.sakrafux.realworld.repository;

import com.sakrafux.realworld.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<TagEntity, Long> {

    @Query("SELECT t.tag FROM TagEntity t")
    List<String> findAllTagNames();

    Optional<TagEntity> findByTag(String tag);
}
