package org.umc.travlocksserver.domain.member.service.query;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.favorite.repository.FavoriteRepository;
import org.umc.travlocksserver.domain.member.exception.MemberException;
import org.umc.travlocksserver.domain.member.code.MemberErrorCode;
import org.umc.travlocksserver.domain.member.repository.MemberRepository;
import org.umc.travlocksserver.domain.template.dto.response.TemplateCursorResponseDTO;
import org.umc.travlocksserver.domain.template.dto.response.TemplateSummaryDTO;
import org.umc.travlocksserver.domain.template.entity.Template;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteTemplateQueryService {

	private final MemberRepository memberRepository;
	private final FavoriteRepository favoriteRepository;

	public TemplateCursorResponseDTO getMyFavoriteTemplates(Long memberId, Long cursor, int limit) {
		memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

		int safeLimit = Math.min(Math.max(limit, 1), 50);

		List<Template> templates = (cursor == null)
			? favoriteRepository.findFavoriteTemplatesFirst(memberId, safeLimit + 1)
			: favoriteRepository.findFavoriteTemplatesAfterCursor(memberId, cursor, safeLimit + 1);

		boolean hasNext = templates.size() > safeLimit;
		if (hasNext) {
			templates = templates.subList(0, safeLimit);
		}

		Long nextCursor = hasNext ? templates.get(templates.size() - 1).getId() : null;

		List<TemplateSummaryDTO> items = templates.stream()
			.map(t -> new TemplateSummaryDTO(
				t.getId(),
				t.getTitle(),
				t.getCoverImageUrl(),
				t.getFavoriteCount(),
				t.getAvgRating(),
				t.getCreatedAt()
			))
			.toList();

		return new TemplateCursorResponseDTO(nextCursor, hasNext, items);
	}
}