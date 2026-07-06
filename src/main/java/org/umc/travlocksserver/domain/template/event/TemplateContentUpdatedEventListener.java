package org.umc.travlocksserver.domain.template.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.umc.travlocksserver.infra.redis.template.TemplateTagGenerationQueue;

@Component
@RequiredArgsConstructor
@Slf4j
public class TemplateContentUpdatedEventListener {

    private final TemplateTagGenerationQueue queue;

    /**
     * 템플릿 수정이 정상적으로 commit된 이후 AI 태그 생성 예약
     * <p>
     * Redis 장애가 발생해도 템플릿 수정은 이미 완료한 상태
    * */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(TemplateContentUpdatedEvent event) {
        try {
            queue.enqueue(event.templateId());
        } catch (Exception e) {
            log.error("AI 태그 생성 예약에 실패했습니다. templateId={} ", event.templateId(), e);
        }
    }
}
