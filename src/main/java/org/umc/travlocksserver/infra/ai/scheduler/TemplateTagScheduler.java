package org.umc.travlocksserver.infra.ai.scheduler;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.umc.travlocksserver.domain.template.repository.TemplateRepository;
import org.umc.travlocksserver.domain.template.service.command.TemplateTagService;
import org.umc.travlocksserver.infra.ai.entity.AiTagExecutionLog;
import org.umc.travlocksserver.infra.ai.repository.AiTagExecutionLogRepository;
import org.umc.travlocksserver.infra.ai.dto.AiTagResponseDTO;
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
	private final EntityManager entityManager;

//	 @Scheduled(cron = "${tag.cron}", zone = "${global.timezone}")
	public void run() {
		// DB 쿼리 수 측정
		SessionFactory sessionFactory = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class);
		Statistics stats = sessionFactory.getStatistics();
		stats.setStatisticsEnabled(true);

		// 처리 전
		stats.clear();
		long start = System.currentTimeMillis();

		int aiCallCount = 0;

		LocalDateTime now = LocalDateTime.now(ZoneId.of(zoneId));
		LocalDateTime to = now.minusMinutes(graceMinutes);
		LocalDateTime from = now.minusMinutes(lookBackMinutes);

		List<Long> templateIds = templateRepository.findRecentlyUpdatedTemplateIds(from, to);

		for (Long templateId : templateIds) {
			try {
				aiCallCount++;
				AiTagResponseDTO result = templateTagService.generateTags(templateId);
			} catch (AiException e) {
				log.error("AI 서비스 오류로 태그 생성 실패 - 템플릿 ID: {}, 사유: {}", templateId, e.getMessage());
			} catch (Exception e) {
				log.error("AI 태그 생성 중 문제가 발생했습니다. - 템플릿 ID: {}, 사유: {}", templateId, e.getMessage(), e);
			}
		}

		long end = System.currentTimeMillis();

		long queryCount = stats.getPrepareStatementCount();
		long entityFetchCount = stats.getEntityFetchCount();

		log.info("DB 조회(PrepareStatement) 건수: {}", queryCount);
		log.info("엔티티 fetch 건수: {}", entityFetchCount);

		AiTagExecutionLog log = AiTagExecutionLog.create(
				start,
				end,
				end - start,
				templateIds.size(),
				aiCallCount,
				(end - start) / templateIds.size(),
				queryCount,
				entityFetchCount
		);

		aiTagExecutionLogRepository.save(log);
	}
}
