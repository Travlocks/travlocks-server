package org.umc.travlocksserver.domain.vlock.entity;

import jakarta.persistence.*;
import lombok.*;
import org.umc.travlocksserver.global.entity.CreatedSoftDeleteBaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "vlock_categories")
public class VlockCategory extends CreatedSoftDeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "vlock_category_id")
    private Long id;

    @Column(nullable = false, length = 10)
    private String name;

    @Column(nullable = false)
    private Float stayHours;
}
