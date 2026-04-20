package com.sakrafux.realworld.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tag")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class TagEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tag_id_gen")
    @SequenceGenerator(name = "tag_id_gen", sequenceName = "seq_tag_id", allocationSize = 1)
    private Long id;

    @EqualsAndHashCode.Include
    @Column(unique = true, nullable = false, length = 20)
    private String tag;

    @ManyToMany(mappedBy = "tags")
    @Builder.Default
    private Set<ArticleEntity> articles = new HashSet<>();
}
