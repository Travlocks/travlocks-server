package org.umc.travlocksserver.domain.template.entity;

import jakarta.persistence.*;
import lombok.*;
import org.umc.travlocksserver.global.entity.CreatedBaseEntity;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "template_days")
public class TemplateDay extends CreatedBaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "template_day_id")
	private Long id;

	/** 어떤 템플릿의 Day인지 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "template_id", nullable = false)
	private Template template;

	/** DAY 1, DAY 2, ... */
	@Column(name = "day_no", nullable = false)
	private Integer dayNo;

	/** 하루 시작 시간 */
	@Column(name = "start_time", nullable = false)
	private LocalTime startTime;

	@Column(name = "vlock_count", nullable = false)
	private Integer vlockCount;

	@OneToMany(mappedBy = "templateDay")
	private List<TemplateVlock> templateVlocks = new ArrayList<>();

	//  vlockCount 증감 메서드 추가
	public void incrementVlockCount() {
		this.vlockCount++;
	}

	public void decrementVlockCount() {
		if (this.vlockCount > 0) {
			this.vlockCount--;
		}
	}
}
