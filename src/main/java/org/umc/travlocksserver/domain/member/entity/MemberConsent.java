package org.umc.travlocksserver.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.umc.travlocksserver.global.entity.CreatedSoftDeleteBaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "member_consents", uniqueConstraints = {
	@UniqueConstraint(name = "uk_member_policy", columnNames = {"member_id", "policy_id"})
})
public class MemberConsent extends CreatedSoftDeleteBaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "member_consent_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "policy_id", nullable = false)
	private Policy policy;

	@Column(name = "is_agreed", nullable = false)
	private boolean agreed;
}
