package org.umc.travlocksserver.domain.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.umc.travlocksserver.domain.auth.enums.OAuthProvider;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.global.entity.CreatedSoftDeleteBaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "oauth_accounts")
public class OAuthAccount extends CreatedSoftDeleteBaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "oauth_account_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private OAuthProvider provider; // GOOGLE, KAKAO, NAVER

	@Column(name = "provider_id", nullable = false, length = 100)
	private String providerId;

}
