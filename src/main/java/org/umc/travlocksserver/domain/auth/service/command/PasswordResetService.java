package org.umc.travlocksserver.domain.auth.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.umc.travlocksserver.domain.auth.dto.response.AuthPasswordResetVerifyResponseDTO;
import org.umc.travlocksserver.domain.auth.exception.AuthException;
import org.umc.travlocksserver.domain.auth.exception.code.AuthErrorCode;
import org.umc.travlocksserver.domain.auth.repository.PasswordResetTokenRedisRepository;
import org.umc.travlocksserver.domain.member.repository.MemberRepository;
import org.umc.travlocksserver.global.mail.ResendMailSender;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final MemberRepository memberRepository;
    private final PasswordResetTokenRedisRepository passwordResetTokenRedisRepository;
    private final ResendMailSender resendMailSender;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Value("${password-reset.redirect-uri}")
    private String redirectUri;

    @Value("${password-reset.ttl-minutes:30}")
    private long ttlMinutes;

    public void sendPasswordResetLink(String email) {
        boolean exists = memberRepository.existsByEmail(email);

        if (!exists) {
            return;
        }

        // resetToken 생성 + Redis 저장
        String resetToken = generateResetToken();
        passwordResetTokenRedisRepository.save(
                resetToken,
                new PasswordResetTokenRedisRepository.PasswordResetCache(email),
                Duration.ofMinutes(ttlMinutes)
        );

        // resetUrl 생성 (token을 query로)
        // 프론트 라우트 예: https://travlocks.kro.kr/reset-password?token=...
        String resetUrl = buildResetUrl(resetToken);

        resendMailSender.sendPasswordResetLink(email, resetUrl, ttlMinutes);
    }

    public AuthPasswordResetVerifyResponseDTO verifyResetToken(String token) {
        if (passwordResetTokenRedisRepository.find(token) == null) {
            throw new AuthException(AuthErrorCode.PASSWORD_RESET_TOKEN_INVALID);
        }

        return new AuthPasswordResetVerifyResponseDTO(true);
    }

    private String buildResetUrl(String token) {
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        if (redirectUri.contains("?")) {
            return redirectUri + "&token=" + encodedToken;
        }
        return redirectUri + "?token=" + encodedToken;
    }

    private String generateResetToken() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return "pwdreset_" + HexFormat.of().formatHex(bytes);
    }
}
