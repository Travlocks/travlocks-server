package org.umc.travlocksserver.infra.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.umc.travlocksserver.domain.template.repository.TemplateRepository;
import org.umc.travlocksserver.domain.template.service.command.TemplateTagService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TemplateTagScheduler {

    @Value("${global.timezone}")
    private String zoneId;

    @Value("${tag.grace-minutes}")
    private int graceMinutes;

    @Value("${tag.lookback-minutes}")
    private int lookBackMinutes;

    private final TemplateRepository templateRepository;
    private final TemplateTagService templateTagService;


    // @Scheduled(cron = "${tag.cron}", zone = "${tag.zone}")
    public void run() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of(zoneId));
        LocalDateTime to = now.minusMinutes(graceMinutes);
        LocalDateTime from = now.minusMinutes(lookBackMinutes);

        List<Long> templateIds = templateRepository.findRecentlyUpdatedTemplateIds(from, to);

        for (Long templateId : templateIds) {
            try {
                templateTagService.generateTags(templateId, now);
            } catch (AiClientException ae) {
                throw new AiClientException("AI 서버에 문제가 발생했습니다.");
            } catch (Exception e) {
                log.warn("AI 태그 생성 중 문제가 발생했습니다." + e.getMessage());
            }
        }
    }
}
