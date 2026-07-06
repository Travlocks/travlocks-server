package org.umc.travlocksserver.domain.template.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.template.entity.Template;
import org.umc.travlocksserver.domain.template.repository.TemplateRepository;
import org.umc.travlocksserver.domain.template.service.command.TemplateTagCommandService;
import org.umc.travlocksserver.infra.redis.template.TemplateTagGenerationQueue;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class TemplateTagGenerationWorker {

    private final TemplateTagGenerationQueue queue;
    private final TemplateRepository templateRepository;
    private final TemplateTagCommandService templateTagCommandService;

    /**
     * 실행시간이 지난 Template에 대해 AI 태그 생성을 호출하는 메서드
    * */
    @Scheduled(fixedDelayString = "${tag.worker-delay}") // 이전 작업이 끝난 후 1분마다 실행
    @Transactional
    public void execute() {
        Set<Long> templateIds = queue.findDueTemplateIds();

        for (Long templateId : templateIds) {
            try {
                Template template = templateRepository.findById(templateId)
                        .orElse(null);

                if (template == null) {
                    queue.remove(templateId);
                    continue;
                }

                templateTagCommandService.generateTags(templateId);
                queue.remove(templateId);
            } catch (Exception e) {
                log.error("AI 태그 생성에 실패했습니다. templateId={} ", templateId, e);
            }
        }
    }
}
