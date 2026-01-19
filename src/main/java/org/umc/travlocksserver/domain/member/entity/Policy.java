package org.umc.travlocksserver.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.umc.travlocksserver.domain.member.enums.PolicyType;
import org.umc.travlocksserver.global.entity.SoftDeleteBaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "policies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Policy extends SoftDeleteBaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 15, nullable = false)
    private PolicyType type; // SERVICE, PRIVACY, MARKETING

    @Column(name = "is_required", nullable = false)
    private boolean required;

}

