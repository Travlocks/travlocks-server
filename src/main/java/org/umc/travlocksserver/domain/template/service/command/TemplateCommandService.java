package org.umc.travlocksserver.domain.template.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.member.service.query.MemberQueryService;
import org.umc.travlocksserver.domain.template.entity.Template;
import org.umc.travlocksserver.domain.template.service.query.TemplateQueryService;

@Service
@RequiredArgsConstructor
@Transactional
public class TemplateCommandService {

    private final TemplateQueryService templateQueryService;
    private final MemberQueryService memberQueryService;

    public void deleteById(Long memberId, Long templateId) {
        Member member = memberQueryService.getById(memberId);
        Template template = templateQueryService.getTemplateByIdAndOwnerId(templateId, memberId);

        member.decreaseTemplateCount();
        template.softDelete();
    }
}
