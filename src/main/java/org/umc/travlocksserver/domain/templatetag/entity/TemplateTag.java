package org.umc.travlocksserver.domain.templatetag.entity;

import jakarta.persistence.*;
import lombok.*;
import org.umc.travlocksserver.domain.tag.entity.Tag;
import org.umc.travlocksserver.domain.template.entity.Template;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "template_tag")
public class TemplateTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_tag_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    @Column(name = "rank", nullable = false)
    private Integer rank;

    @Column(name = "confidence", nullable = false)
    private Double confidence;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
