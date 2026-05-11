package com.sakrafux.realworld.features.comment;

import com.sakrafux.realworld.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "comment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class CommentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "fk_article", nullable = false)
    private Long articleId;

    @Column(name = "fk_author", nullable = false)
    private Long authorId;

    public static List<CommentEntity> findByArticleId(Long articleId) {
        return find("articleId", articleId).list();
    }
}
