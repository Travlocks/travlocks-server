package org.umc.travlocksserver.global.jwt;

public interface JwtTokenProvider {

    String generateAccessToken(Long memberId);
    String generateRefreshToken(Long memberId, String jti);

    long getAccessTokenExpiresInSeconds();

    // refreshToken에서 jti 꺼내기
    String extractJti(String refreshToken);
    Long extractMemberId(String refreshToken);

    // refreshToken 유효성 검증
    boolean validateRefreshToken(String refreshToken);
}
