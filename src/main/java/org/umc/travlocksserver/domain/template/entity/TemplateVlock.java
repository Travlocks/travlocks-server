package org.umc.travlocksserver.domain.template.entity;

import jakarta.persistence.*;
import lombok.*;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;
import org.umc.travlocksserver.global.entity.BaseEntity;
import org.umc.travlocksserver.domain.template.enums.ConnectionPortType;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "template_vlocks")
public class TemplateVlock extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "template_vlocks_id")
	private Long id;

	/** 어느 DAY에 속한 블록인지 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "template_day_id", nullable = false)
	private TemplateDay templateDay;

	/** 어떤 블록인지 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vlock_id", nullable = false)
	private Vlock vlock;

	/** DAY 내 순서 */
	@Column(name = "order_no", nullable = false)
	private Integer orderNo;

	/** 체류 시간 (시간) */
	@Column(name = "stay_hours", nullable = false)
	private Double stayHours;

	// 레이아웃 정보
	@Column(name = "canvas_x")
	private Double canvasX;

	@Column(name = "canvas_y")
	private Double canvasY;

	//  입력 포트 (이전 블록에서 받음)
	@Enumerated(EnumType.STRING)
	@Column(name = "input_port", length = 20)
	private ConnectionPortType inputPort;

	//  출력 포트 (다음 블록으로 보냄)
	@Enumerated(EnumType.STRING)
	@Column(name = "output_port", length = 20)
	private ConnectionPortType outputPort;

	// 메서드
	public void updateOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	public void updateLayout(
		Double canvasX,
		Double canvasY,
		ConnectionPortType inputPort,
		ConnectionPortType outputPort) {
		this.canvasX = canvasX;
		this.canvasY = canvasY;
		this.inputPort = inputPort;
		this.outputPort = outputPort;
	}
}
