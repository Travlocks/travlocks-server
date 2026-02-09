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
	@Column(name = "vlock_category_id")
    private Long id;

    @Column(nullable = false, length = 10)
    private String name;

    @Column(nullable = false)
    private Double stayHours;

	@Column(name = "default_creation_image_key", nullable = false)
	private String defaultCreationImageKey;

	@Column(name = "default_category_image_key", nullable = false)
	private String defaultCategoryImageKey;
}
