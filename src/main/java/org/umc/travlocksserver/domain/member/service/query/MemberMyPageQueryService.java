package org.umc.travlocksserver.domain.member.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.member.dto.response.CreatedTemplateDTO;
import org.umc.travlocksserver.domain.member.dto.response.CreatedVlockDTO;
import org.umc.travlocksserver.domain.member.dto.response.MemberMyPageResponseDTO;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.member.repository.MemberRepository;
import org.umc.travlocksserver.domain.template.dto.response.TemplateCardResponseDTO;
import org.umc.travlocksserver.domain.template.repository.TemplateRepository;
import org.umc.travlocksserver.domain.travelstyle.repository.PreferredTravelStyleRepository;
import org.umc.travlocksserver.domain.traveltheme.repository.PreferredTravelThemeRepository;
import org.umc.travlocksserver.domain.vlock.repository.VlockRepository;
import org.umc.travlocksserver.global.aws.S3Properties;
import org.umc.travlocksserver.global.response.PageResponseDTO;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberMyPageQueryService {

    private final MemberRepository memberRepository;
    private final PreferredTravelStyleRepository preferredTravelStyleRepository;
    private final PreferredTravelThemeRepository preferredTravelThemeRepository;
    private final VlockRepository vlockRepository;
    private final TemplateRepository templateRepository;
	private final S3Properties s3Properties;

    @Transactional(readOnly = true)
    public MemberMyPageResponseDTO getMyPage(Member member) {
        Long memberId = member.getId();

        List<Long> styleIds = preferredTravelStyleRepository.findPreferredStyleIdsByMemberId(memberId);
        List<Long> themeIds = preferredTravelThemeRepository.findPreferredThemeIdsByMemberId(memberId);

        List<CreatedVlockDTO> vlocks = vlockRepository.findRecentCreatedVlocks(memberId, 4, s3Properties.domain());
        List<CreatedTemplateDTO> templates = templateRepository.findRecentCreatedTemplates(memberId, 4);

        return new MemberMyPageResponseDTO(
                member.getId(),
                member.getNickname(),
                member.getIntroduction(),
                styleIds,
                themeIds,
                new MemberMyPageResponseDTO.Counts(
                        member.getVlockCount(),
                        member.getTemplateCount(),
                        member.getStarCount()
                ),
                new MemberMyPageResponseDTO.Recent(vlocks, templates)
        );
    }

    public PageResponseDTO<TemplateCardResponseDTO> getMyTemplates(Long memberId, Pageable pageable) {
        Page<TemplateCardResponseDTO> response = templateRepository.findMyTemplates(memberId, pageable);
        return PageResponseDTO.from(response);
    }
}
