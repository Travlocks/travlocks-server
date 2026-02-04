package org.umc.travlocksserver.domain.template.service.query;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.template.code.TemplateErrorCode;
import org.umc.travlocksserver.domain.template.dto.response.TemplateCanvasResponseDTO;
import org.umc.travlocksserver.domain.template.dto.response.TemplateCanvasVlockDTO;
import org.umc.travlocksserver.domain.template.entity.Template;
import org.umc.travlocksserver.domain.template.entity.TemplateDay;
import org.umc.travlocksserver.domain.template.entity.TemplateVlock;
import org.umc.travlocksserver.domain.template.enums.TransportType;
import org.umc.travlocksserver.domain.template.exception.TemplateException;
import org.umc.travlocksserver.domain.template.repository.TemplateDayRepository;
import org.umc.travlocksserver.domain.template.repository.TemplateRepository;
import org.umc.travlocksserver.domain.template.repository.TemplateVlockRepository;
import org.umc.travlocksserver.domain.vlock.dto.response.VlockBriefDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TemplateCanvasQueryService {

	private final TemplateRepository templateRepository;
	private final TemplateDayRepository templateDayRepository;
	private final TemplateVlockRepository templateVlockRepository;

	private final TemplateRouteQueryService templateRouteQueryService;

	public TemplateCanvasResponseDTO getTemplateCanvas(Long templateId, Integer dayNo) {

		Template template = templateRepository.findById(templateId)
			.orElseThrow(() -> new TemplateException(TemplateErrorCode.TEMPLATE_NOT_FOUND));

		TemplateDay day = templateDayRepository.findByTemplateIdAndDayNo(templateId, dayNo)
			.orElseThrow(() -> new TemplateException(TemplateErrorCode.TEMPLATE_CANVAS_NOT_FOUND));

		List<TemplateVlock> templateVlocks = templateVlockRepository.findAllByTemplateDayIdFetchVlock(day.getId());

		double totalStayHours = 0;
		int totalMoveMinutes = 0;

		List<TemplateCanvasVlockDTO> vlocks = new ArrayList<>(templateVlocks.size());

		for (int i = 0; i < templateVlocks.size(); i++) {
			TemplateVlock cur = templateVlocks.get(i);
			TemplateVlock next = (i + 1 < templateVlocks.size()) ? templateVlocks.get(i + 1) : null;

			int nextMoveMinutes = (next == null)
				? 0 : calcNextMoveMinutes(cur.getVlock().getId(), next.getVlock().getId());

			totalStayHours += cur.getStayHours();
			totalMoveMinutes += nextMoveMinutes;

			vlocks.add(new TemplateCanvasVlockDTO(
				cur.getId(),
				cur.getOrderNo(),
				cur.getStayHours(),
				nextMoveMinutes,
				new VlockBriefDTO(
					cur.getVlock().getId(),
					cur.getVlock().getName(),
					cur.getVlock().getVlockCategory().getName()
				)
			));
		}

		double totalHours = totalStayHours + (double) totalMoveMinutes / 60;

		return new TemplateCanvasResponseDTO(
			template.getId(),
			template.getTitle(),
			dayNo,
			day.getVlockCount(),
			totalHours,
			totalMoveMinutes,
			totalStayHours,
			vlocks,
			template.getCreatedAt()
		);
	}

	/**
	 * 블록 간 이동 시간 계산 (MoveTime 재사용, 없으면 생성)
	 */
	private int calcNextMoveMinutes(Long fromVlockId, Long toVlockId) {
		var route = templateRouteQueryService.getOrCreateRoute(fromVlockId, toVlockId, TransportType.WALK);
		return route.moveMinutes();
	}
}
