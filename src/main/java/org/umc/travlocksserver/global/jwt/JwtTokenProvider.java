package org.umc.travlocksserver.global.jwt;

public interface JwtTokenProvider {

    String generateAccessToken(Long memberId);
    String generateRefreshToken(Long memberId, String jti);

    long getAccessTokenExpiresInSeconds();

    String extractJti(String refreshToken);
    Long extractMemberId(String refreshToken);
    boolean validateRefreshToken(String refreshToken);

    boolean validateAccessToken(String accessToken);
    Long extractMemberIdFromAccessToken(String accessToken);
}
