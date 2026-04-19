package com.sakrafux.realworld.entity;

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
public class ArticleEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "article_id_gen")
    @SequenceGenerator(name = "article_id_gen", sequenceName = "seq_article_id", allocationSize = 1)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String slug;

    @Column(unique = true, nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_author", nullable = false)
    private UserEntity author;

    @ManyToMany
    @JoinTable(
        name = "tag_is_article_to_tag",
        joinColumns = @JoinColumn(name = "article_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<TagEntity> tags = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "favorite_is_article_to_user",
        joinColumns = @JoinColumn(name = "article_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<UserEntity> favoritedBy = new HashSet<>();

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CommentEntity> comments = new ArrayList<>();
}
