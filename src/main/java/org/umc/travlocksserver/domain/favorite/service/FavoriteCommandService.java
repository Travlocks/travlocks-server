package org.umc.travlocksserver.domain.favorite.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.favorite.code.FavoriteErrorCode;
import org.umc.travlocksserver.domain.favorite.entity.Favorite;
import org.umc.travlocksserver.domain.favorite.exception.FavoriteException;
import org.umc.travlocksserver.domain.favorite.repository.FavoriteRepository;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.member.exception.MemberException;
import org.umc.travlocksserver.domain.member.code.MemberErrorCode;
import org.umc.travlocksserver.domain.member.repository.MemberRepository;
import org.umc.travlocksserver.domain.template.code.TemplateErrorCode;
import org.umc.travlocksserver.domain.template.entity.Template;
import org.umc.travlocksserver.domain.template.repository.TemplateRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class FavoriteCommandService {

    private final FavoriteRepository favoriteRepository;
    private final TemplateRepository templateRepository;
    private final MemberRepository memberRepository;

    /**
     * 즐겨찾기 추가
     */
    public void addFavorite(Long memberId, Long templateId) {

        // 템플릿 조회
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new FavoriteException(TemplateErrorCode.TEMPLATE_NOT_FOUND));

        // 이미 즐겨찾기 되어 있는지 확인
        if (favoriteRepository.existsByMemberIdAndTemplateId(memberId, templateId)) {
            throw new FavoriteException(FavoriteErrorCode.ALREADY_FAVORITED);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        Favorite favorite = Favorite.builder()
                .member(member)
                .template(template)
                .build();

        favoriteRepository.save(favorite);

        //템플릿 즐겨찾기 수 증가
        template.increaseFavoriteCount();
    }

    /**
     * 즐겨찾기 취소
     */
    public void removeFavorite(Long memberId, Long templateId) {

        // Favorite 조회
        Favorite favorite = favoriteRepository.findByMemberIdAndTemplateId(memberId, templateId)
                .orElseThrow(() -> new FavoriteException(FavoriteErrorCode.FAVORITE_NOT_FOUND));

        // 템플릿 조회
        Template template = favorite.getTemplate();

        // Favorite 삭제
        favoriteRepository.delete(favorite);

        // 템플릿 즐겨찾기 수 감소
        template.decreaseFavoriteCount();
    }
}