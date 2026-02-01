package org.umc.travlocksserver.domain.favorite.controller;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.umc.travlocksserver.domain.favorite.code.FavoriteSuccessCode;
import org.umc.travlocksserver.domain.favorite.service.FavoriteCommandService;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.global.annotation.LoginUser;
import org.umc.travlocksserver.global.response.SuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/templates")
public class FavoriteController implements FavoriteControllerDocs {

    private final FavoriteCommandService favoriteCommandService;

    /**
     * 즐겨찾기 추가
     */
    @PutMapping("/{templateId}/favorite")
    @Override
    public ResponseEntity<SuccessResponse<Void>> addFavorite(
            @PathVariable Long templateId,
            @Parameter(hidden = true) @LoginUser Member member
    ) {
        Long memberId = member.getId();
        favoriteCommandService.addFavorite(memberId, templateId);
        return ResponseEntity.ok(
                SuccessResponse.ok(FavoriteSuccessCode.FAVORITE_ADD_SUCCESS)
        );
    }

    /**
     * 즐겨찾기 취소
     */
    @DeleteMapping("/{templateId}/favorite")
    @Override
    public ResponseEntity<SuccessResponse<Void>> removeFavorite(
            @PathVariable Long templateId,
            @Parameter(hidden = true) @LoginUser Member member
    ) {
        Long memberId = member.getId();
        favoriteCommandService.removeFavorite(memberId, templateId);
        return ResponseEntity.ok(
                SuccessResponse.ok(FavoriteSuccessCode.FAVORITE_REMOVE_SUCCESS)
        );
    }
}