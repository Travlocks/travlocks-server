package org.umc.travlocksserver.infra.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.umc.travlocksserver.domain.template.repository.TemplateRepository;
import org.umc.travlocksserver.domain.template.service.command.TemplateTagService;
import org.umc.travlocksserver.infra.ai.exception.AiException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TemplateTagScheduler {

	private final AiTagExecutionLogRepository aiTagExecutionLogRepository;

	@Value("${global.timezone}")
	private String zoneId;

	@Value("${tag.grace-minutes}")
	private int graceMinutes;

	@Value("${tag.lookback-minutes}")
	private int lookBackMinutes;

	private final TemplateRepository templateRepository;
	private final TemplateTagService templateTagService;

//	 @Scheduled(cron = "${tag.cron}", zone = "${global.timezone}")
	public void run() {
		long start = System.currentTimeMillis();

		int aiCallCount = 0;
		long totalAiProcessingTimeMs = 0;

		LocalDateTime now = LocalDateTime.now(ZoneId.of(zoneId));
		LocalDateTime to = now.minusMinutes(graceMinutes);
		LocalDateTime from = now.minusMinutes(lookBackMinutes);

		List<Long> templateIds = templateRepository.findRecentlyUpdatedTemplateIds(from, to);

		for (Long templateId : templateIds) {
			try {
				aiCallCount++;

				AiResult result = templateTagService.generateTags(templateId, now);
				totalAiProcessingTimeMs += result.aiLatencyMs();
			} catch (AiException e) {
				log.error("AI 서비스 오류로 태그 생성 실패 - 템플릿 ID: {}, 사유: {}", templateId, e.getMessage());
			} catch (Exception e) {
				log.error("AI 태그 생성 중 문제가 발생했습니다. - 템플릿 ID: {}, 사유: {}", templateId, e.getMessage());
			}
		}

		long end = System.currentTimeMillis();

		AiTagExecutionLog log = AiTagExecutionLog.create(
				start,
				end,
				end - start,
				templateIds.size(),
				aiCallCount,
				totalAiProcessingTimeMs,
				totalAiProcessingTimeMs / aiCallCount
		);

		aiTagExecutionLogRepository.save(log);
	}
}
