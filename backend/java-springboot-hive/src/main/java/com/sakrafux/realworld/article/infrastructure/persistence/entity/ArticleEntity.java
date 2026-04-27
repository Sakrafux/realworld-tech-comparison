package com.sakrafux.realworld.article.infrastructure.persistence.entity;

import com.sakrafux.realworld.core.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "article")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class ArticleEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "article_id_gen")
    @SequenceGenerator(name = "article_id_gen", sequenceName = "seq_article_id", allocationSize = 1)
    private Long id;

    @EqualsAndHashCode.Include
    @Column(unique = true, nullable = false, length = 100)
    private String slug;

    @Column(unique = true, nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "fk_author", nullable = false)
    private Long authorId;

    @ManyToMany
    @JoinTable(
        name = "tag_is_article_to_tag",
        joinColumns = @JoinColumn(name = "article_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<TagEntity> tags = new HashSet<>();

    @ElementCollection
    @CollectionTable(
        name = "favorite_is_article_to_user",
        joinColumns = @JoinColumn(name = "article_id")
    )
    @Column(name = "user_id")
    @Builder.Default
    private Set<Long> favoritedByUserIds = new HashSet<>();

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CommentEntity> comments = new ArrayList<>();
}
