package org.umc.travlocksserver.domain.template.entity;

import jakarta.persistence.*;
import lombok.*;
import org.umc.travlocksserver.domain.template.enums.TagType;
import org.umc.travlocksserver.global.entity.CreatedBaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "template_tags")
public class TemplateTag extends CreatedBaseEntity {

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

	@Column(nullable = false, length = 16)
	private TagType type;

	@Column(name = "version", nullable = false)
	private Long version;

	public static TemplateTag create(Tag tag, Template template, TagType type, Long version) {
		return TemplateTag.builder()
			.tag(tag)
			.template(template)
			.type(type)
			.version(version)
			.build();
	}
}
