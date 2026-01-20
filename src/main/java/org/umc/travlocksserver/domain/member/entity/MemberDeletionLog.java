package org.umc.travlocksserver.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.umc.travlocksserver.global.entity.CreatedBaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "member_deletion_logs")
public class MemberDeletionLog extends CreatedBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_deletion_log_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(length = 255)
    private String reason;
}
