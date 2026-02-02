package org.umc.travlocksserver.domain.favorite.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.global.response.SuccessResponse;

@Tag(name = "Favorite", description = "템플릿 즐겨찾기 API")
public interface FavoriteControllerDocs {

    @Operation(
            summary = "템플릿 즐겨찾기 추가",
            description = "로그인한 사용자가 템플릿을 즐겨찾기에 추가합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "즐겨찾기 추가 성공"),
            @ApiResponse(responseCode = "400", description = "이미 즐겨찾기한 템플릿"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 템플릿"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    ResponseEntity<SuccessResponse<Void>> addFavorite(
            Long templateId,
            Member member
    );

    @Operation(
            summary = "템플릿 즐겨찾기 취소",
            description = "로그인한 사용자가 템플릿 즐겨찾기를 취소합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "즐겨찾기 취소 성공"),
            @ApiResponse(responseCode = "404", description = "즐겨찾기하지 않은 템플릿"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    ResponseEntity<SuccessResponse<Void>> removeFavorite(
            Long templateId,
            Member member
    );
}
