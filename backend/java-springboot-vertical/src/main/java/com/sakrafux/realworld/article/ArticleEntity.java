package com.sakrafux.realworld.article;

import com.sakrafux.realworld.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
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

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @ElementCollection
    @CollectionTable(name = "article_tags", joinColumns = @JoinColumn(name = "article_id"))
    @Column(name = "tag_name")
    @Builder.Default
    private Set<String> tags = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "article_favorites", joinColumns = @JoinColumn(name = "article_id"))
    @Column(name = "user_id")
    @Builder.Default
    private Set<Long> favoritedBy = new HashSet<>();
}
