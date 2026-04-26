package com.sakrafux.realworld.infrastructure.adapter.out.persistence;

import com.sakrafux.realworld.domain.model.Comment;
import com.sakrafux.realworld.domain.model.Profile;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.entity.ArticleEntity;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.entity.CommentEntity;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.mapper.CommentPersistenceMapper;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.repository.ArticleJpaRepository;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.repository.CommentJpaRepository;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.repository.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentPersistenceAdapterTest {

    @Mock
    private CommentJpaRepository commentJpaRepository;
    @Mock
    private ArticleJpaRepository articleJpaRepository;
    @Mock
    private UserJpaRepository userJpaRepository;
    @Mock
    private CommentPersistenceMapper commentMapper;

    @InjectMocks
    private CommentPersistenceAdapter commentPersistenceAdapter;

    @Test
    void save_validData_mapsAndSaves() {
        // Given
        Comment comment = Comment.builder().author(Profile.builder().username("author").build()).build();
        String slug = "slug";
        ArticleEntity articleEntity = new ArticleEntity();
        UserEntity authorEntity = UserEntity.builder().username("author").build();
        CommentEntity commentEntity = new CommentEntity();

        given(articleJpaRepository.findBySlug(slug)).willReturn(Optional.of(articleEntity));
        given(userJpaRepository.findByUsername("author")).willReturn(Optional.of(authorEntity));
        given(commentMapper.toEntity(comment)).willReturn(commentEntity);
        given(commentJpaRepository.save(commentEntity)).willReturn(commentEntity);
        given(commentMapper.toDomain(commentEntity)).willReturn(comment);

        // When
        Comment result = commentPersistenceAdapter.save(comment, slug);

        // Then
        assertThat(result).isNotNull();
        verify(commentJpaRepository).save(commentEntity);
    }

    @Test
    void findByArticleSlug_exists_returnsComments() {
        // Given
        String slug = "slug";
        ArticleEntity articleEntity = new ArticleEntity();
        CommentEntity commentEntity = new CommentEntity();
        given(articleJpaRepository.findBySlug(slug)).willReturn(Optional.of(articleEntity));
        given(commentJpaRepository.findByArticleOrderByCreatedAtDesc(articleEntity)).willReturn(List.of(commentEntity));
        given(commentMapper.toDomain(commentEntity)).willReturn(Comment.builder().build());

        // When
        List<Comment> result = commentPersistenceAdapter.findByArticleSlug(slug);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void delete_validId_callsRepository() {
        // Given
        Long id = 1L;

        // When
        commentPersistenceAdapter.delete(id);

        // Then
        verify(commentJpaRepository).deleteById(id);
    }
}
