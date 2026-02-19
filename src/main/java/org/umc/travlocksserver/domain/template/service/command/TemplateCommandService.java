package org.umc.travlocksserver.domain.template.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.umc.travlocksserver.domain.template.code.TemplateErrorCode;
import org.umc.travlocksserver.domain.template.dto.request.TemplateSaveRequestDTO;
import org.umc.travlocksserver.domain.template.dto.response.TemplateSaveResponseDTO;
import org.umc.travlocksserver.domain.template.entity.Template;
import org.umc.travlocksserver.domain.template.exception.TemplateException;
import org.umc.travlocksserver.domain.template.repository.TemplateRepository;
import org.umc.travlocksserver.domain.vlock.service.command.VlockExternalCommandService;
import org.umc.travlocksserver.global.aws.S3Provider;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.member.service.query.MemberQueryService;
import org.umc.travlocksserver.domain.template.service.query.TemplateQueryService;

@Service
@RequiredArgsConstructor
@Transactional
public class TemplateCommandService {

	private final TemplateRepository templateRepository;
	private final S3Provider s3Provider;
	private final TemplateQueryService templateQueryService;
	private final MemberQueryService memberQueryService;
	private final TemplateTagCommandService templateTagCommandService;
	private final VlockExternalCommandService vlockExternalCommandService;

	public TemplateSaveResponseDTO saveTemplate(
		Long memberId,
		Long templateId,
		TemplateSaveRequestDTO request,
		MultipartFile coverImage) {

		// 1. 템플릿 조회
		Template template = templateRepository.findById(templateId)
			.orElseThrow(() -> new TemplateException(TemplateErrorCode.TEMPLATE_NOT_FOUND));

		// 2. 권한 검증
		if (!template.getOwner().getId().equals(memberId)) {
			throw new TemplateException(TemplateErrorCode.TEMPLATE_FORBIDDEN);
		}

		// 3. 커버 이미지 업로드
		String newImageUrl = null;
		if (coverImage != null && !coverImage.isEmpty()) {
			newImageUrl = s3Provider.uploadTemplateFile(coverImage);

			if (template.getCoverImageUrl() != null) {
				s3Provider.deleteFile(template.getCoverImageUrl());
			}
		}

		// 4. 메타데이터 업데이트
		template.updateMetadata(
			request.title(),
			request.description(),
			newImageUrl,
			request.isPublic());

		// 5. 최초 저장 시 태그 생성 (블록 3개 미만인 경우 AI 태그 외 고정 태그만 생성)
		if (template.getTagVersion() == 0) {
			templateTagCommandService.generateTags(templateId);
		}

		// 더미데이터용
		vlockExternalCommandService.fetchFromExternal(templateId, null, null);

		return TemplateSaveResponseDTO.from(template);
	}

	public void deleteById(Long memberId, Long templateId) {
		Member member = memberQueryService.getById(memberId);
		Template template = templateQueryService.getTemplateByIdAndOwnerId(templateId, memberId);

		member.decreaseTemplateCount();
		template.softDelete();
	}
}
