package org.umc.travlocksserver.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.umc.travlocksserver.domain.member.enums.MemberStatus;
import org.umc.travlocksserver.global.entity.SoftDeleteBaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "members",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_members_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_members_nickname", columnNames = "nickname")
        })
public class Member extends SoftDeleteBaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Getter(AccessLevel.NONE)
    @Column(name = "password_hash", length = 255)
    private String passwordHash; // OAuth면 NULL 가능

    @Column(name = "nickname", length = 10, nullable = false)
    private String nickname;

    @Column(name = "introduction", length = 500)
    private String introduction;

    @Column(name = "email", length = 255, nullable = false)
    private String email;

	@Column(name = "profile_image_url", length = 255)
	private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10, nullable = false)
    private MemberStatus status;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "vlock_count", nullable = false)
    private int vlockCount;

    @Column(name = "template_count", nullable = false)
    private int templateCount;

    @Column(name = "star_count", nullable = false)
    private int starCount;

    public static Member of(Long id) {
        Member member = new Member();
        member.id = id;
        return member;
    }

    public boolean matchesPassword(PasswordEncoder encoder, String rawPassword) {
        if (this.passwordHash == null) {
            return false; // OAuth 계정 대비
        }
        return encoder.matches(rawPassword, this.passwordHash);
    }

    public void changePassword(String encodedPassword) {
        this.passwordHash = encodedPassword;
    }

}