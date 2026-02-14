package org.umc.travlocksserver.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public abstract class SoftDeleteBaseEntity extends BaseEntity {

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	public void softDelete() {
		this.deletedAt = LocalDateTime.now();
	}

	public boolean isDeleted() {
		return this.deletedAt != null;
	}
}
