package org.umc.travlocksserver.domain.favorite.entity;

import jakarta.persistence.*;
import lombok.*;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.template.entity.Template;
import org.umc.travlocksserver.global.entity.CreatedBaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "favorites", uniqueConstraints = {
	@UniqueConstraint(columnNames = {"member_id", "template_id"})
})
public class Favorite extends CreatedBaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "favorite_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "template_id", nullable = false)
	private Template template;

}
