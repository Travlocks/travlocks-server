package org.umc.travlocksserver.domain.template.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.template.code.TemplateErrorCode;
import org.umc.travlocksserver.domain.template.dto.response.TemplateSummaryResponseDTO;
import org.umc.travlocksserver.domain.template.dto.response.TemplateSummaryResponseDTO.DaySummaryDTO;
import org.umc.travlocksserver.domain.template.entity.Template;
import org.umc.travlocksserver.domain.template.entity.TemplateDay;
import org.umc.travlocksserver.domain.template.entity.TemplateVlock;
import org.umc.travlocksserver.domain.template.exception.TemplateException;
import org.umc.travlocksserver.domain.template.repository.MoveTimeRepository;
import org.umc.travlocksserver.domain.template.repository.TemplateDayRepository;
import org.umc.travlocksserver.domain.template.repository.TemplateRepository;
import org.umc.travlocksserver.domain.template.repository.TemplateVlockRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TemplateSummaryQueryService {

	private final TemplateRepository templateRepository;
	private final TemplateDayRepository templateDayRepository;
	private final TemplateVlockRepository templateVlockRepository;
	private final MoveTimeRepository moveTimeRepository;

	private static final int MOVE_WARNING_THRESHOLD = 30;

	public TemplateSummaryResponseDTO getSummary(Long templateId) {
		// 1. 템플릿 조회
		Template template = templateRepository.findById(templateId)
			.orElseThrow(() -> new TemplateException(TemplateErrorCode.TEMPLATE_NOT_FOUND));

		// 2. 일자 목록 조회
		List<TemplateDay> days = templateDayRepository.findByTemplateIdOrderByDayNoAsc(templateId);

		int totalVlocks = 0;
		double totalStayHours = 0.0;
		int totalMoveMinutes = 0;
		List<DaySummaryDTO> daysSummary = new ArrayList<>();

		for (TemplateDay day : days) {
			// 3. 블록 조회 (orderNo 순, join fetch로 vlock까지)
			List<TemplateVlock> vlocks = templateVlockRepository
				.findAllByTemplateDayIdFetchVlock(day.getId());

			// 4. 체류시간 합산 (그냥 더하기!)
			double dayStayHours = vlocks.stream()
				.mapToDouble(TemplateVlock::getStayHours)
				.sum();

			// 5. 이동시간 합산 (연속 블록 쌍, 템플릿 교통수단 기준)
			int dayMoveMinutes = 0;
			List<String> warnings = new ArrayList<>();

			for (int i = 0; i < vlocks.size() - 1; i++) {
				Long fromId = vlocks.get(i).getVlock().getId();
				Long toId = vlocks.get(i + 1).getVlock().getId();

				int moveMin = moveTimeRepository
					.findByFromVlockIdAndToVlockIdAndTransportType(
						fromId, toId, template.getTransportType())
					.map(mt -> mt.getMoveMinutes())
					.orElse(0); // move_times에 없으면 0

				dayMoveMinutes += moveMin;

				if (moveMin >= MOVE_WARNING_THRESHOLD) {
					warnings.add("이동 시간이 30분 이상인 구간이 있습니다.");
				}
			}

			daysSummary.add(new DaySummaryDTO(
				day.getId(),
				day.getDayNo(),
				vlocks.size(),
				dayStayHours,
				dayMoveMinutes,
				warnings.stream().distinct().toList()));

			totalVlocks += vlocks.size();
			totalStayHours += dayStayHours;
			totalMoveMinutes += dayMoveMinutes;
		}

		return new TemplateSummaryResponseDTO(
			templateId,
			totalVlocks,
			totalStayHours,
			totalMoveMinutes,
			daysSummary);
	}
}
