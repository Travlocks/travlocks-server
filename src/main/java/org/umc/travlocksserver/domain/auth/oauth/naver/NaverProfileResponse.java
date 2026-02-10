package org.umc.travlocksserver.domain.auth.oauth.naver;

public record NaverProfileResponse(
        String resultcode,
        String message,
        NaverProfile response
) {
    public record NaverProfile(
            String id,
            String email
    ) {}
}
