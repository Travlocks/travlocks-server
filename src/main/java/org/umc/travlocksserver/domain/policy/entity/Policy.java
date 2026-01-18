package org.umc.travlocksserver.domain.policy.entity;

import jakarta.persistence.*;
import lombok.*;
import org.umc.travlocksserver.global.entity.SoftDeleteBaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "policies")
public class Policy extends SoftDeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id")
    private Long id;

    @Column(name = "type", nullable = false, length = 15)
    private String type; // SERVICE, PRIVACY, MARKETING

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired;
}
